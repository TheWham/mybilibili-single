import json
import logging
import os
import sys
import time
from datetime import datetime
from pathlib import Path

import ollama
import redis
from elasticsearch import Elasticsearch, helpers
from faster_whisper import WhisperModel


QUEUE_KEY = os.getenv("EASYLIVE_AI_QUEUE", "easylive:queue:ai:subtitle-vector")
REDIS_URL = os.getenv("EASYLIVE_REDIS_URL", "redis://127.0.0.1:6379/0")
ES_URL = os.getenv("EASYLIVE_ES_URL", "http://127.0.0.1:9201")
ES_INDEX = os.getenv("EASYLIVE_ES_INDEX", "easylive_video_subtitle_vector")
EMBEDDING_MODEL = os.getenv("EASYLIVE_EMBEDDING_MODEL", "bge-m3:567m")
WHISPER_MODEL = os.getenv("EASYLIVE_WHISPER_MODEL", "small")
WHISPER_DEVICE = os.getenv("EASYLIVE_WHISPER_DEVICE", "cuda")
WHISPER_COMPUTE_TYPE = os.getenv("EASYLIVE_WHISPER_COMPUTE_TYPE", "float16")
REDIS_BLOCK_SECONDS = int(os.getenv("EASYLIVE_REDIS_BLOCK_SECONDS", "5"))


logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s [%(name)s] %(message)s",
)
logger = logging.getLogger("easylive-ai-worker")


def patch_nvidia_libs():
    """Windows 下让 faster-whisper 能找到 pip 安装的 NVIDIA DLL。"""
    site_packages = os.path.join(os.path.dirname(sys.executable), "Lib", "site-packages")
    nvidia_base = os.path.join(site_packages, "nvidia")
    if not os.path.exists(nvidia_base):
        return

    for root, dirs, _files in os.walk(nvidia_base):
        if "bin" not in dirs:
            continue
        bin_path = os.path.join(root, "bin")
        os.environ["PATH"] = bin_path + os.pathsep + os.environ["PATH"]
        if hasattr(os, "add_dll_directory"):
            os.add_dll_directory(bin_path)


def build_redis_client():
    return redis.Redis.from_url(REDIS_URL, decode_responses=True)


def build_es_client():
    return Elasticsearch(ES_URL)


def get_text_embedding(text):
    response = ollama.embeddings(model=EMBEDDING_MODEL, prompt=text)
    vector = response.get("embedding")
    if not vector:
        raise RuntimeError("Ollama 没有返回 embedding")
    return [round(float(value), 5) for value in vector]


def transcribe_and_embed(model, task):
    source_video = Path(task["sourceVideoPath"])
    if not source_video.exists():
        raise FileNotFoundError(f"源视频不存在: {source_video}")

    logger.info("开始提取字幕, videoId=%s, fileId=%s, path=%s", task["videoId"], task["fileId"], source_video)
    segments, info = model.transcribe(str(source_video), beam_size=5)
    logger.info(
        "字幕识别语言=%s, 置信度=%.2f, videoId=%s, fileId=%s",
        info.language,
        info.language_probability,
        task["videoId"],
        task["fileId"],
    )

    docs = []
    for segment_index, segment in enumerate(segments):
        text = segment.text.strip()
        if not text:
            continue

        vector = get_text_embedding(text)
        docs.append(
            {
                "_index": ES_INDEX,
                "_id": f"{task['videoId']}_{task['fileId']}_{segment_index}",
                "_source": {
                    "videoId": task["videoId"],
                    "fileId": task["fileId"],
                    "userId": task.get("userId"),
                    "fileIndex": task.get("fileIndex"),
                    "segmentIndex": segment_index,
                    "videoName": task.get("videoName"),
                    "videoCover": task.get("videoCover"),
                    "tags": task.get("tags"),
                    "content": text,
                    "startTime": round(float(segment.start), 2),
                    "endTime": round(float(segment.end), 2),
                    "contentVector": vector,
                    "embeddingModel": EMBEDDING_MODEL,
                    "createTime": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
                },
            }
        )
    return docs


def index_docs(es_client, docs):
    if not docs:
        return 0
    success_count, errors = helpers.bulk(es_client, docs, raise_on_error=False)
    if errors:
        raise RuntimeError(f"写入 ES 部分失败: {errors[:3]}")
    return success_count


def delete_source_video(task):
    source_video = Path(task["sourceVideoPath"])
    if source_video.exists():
        source_video.unlink()
        logger.info("已删除字幕处理源文件, path=%s", source_video)


def handle_task(model, es_client, task_json):
    task = json.loads(task_json)
    start = time.time()
    docs = transcribe_and_embed(model, task)
    success_count = index_docs(es_client, docs)

    # 到这里说明 ES 写入已经成功，保留的 temp.mp4 可以释放掉。
    delete_source_video(task)
    logger.info(
        "字幕向量化完成, videoId=%s, fileId=%s, segments=%s, cost=%.2fs",
        task.get("videoId"),
        task.get("fileId"),
        success_count,
        time.time() - start,
    )


def main():
    patch_nvidia_libs()
    redis_client = build_redis_client()
    es_client = build_es_client()

    logger.info(
        "加载 Whisper 模型, model=%s, device=%s, computeType=%s",
        WHISPER_MODEL,
        WHISPER_DEVICE,
        WHISPER_COMPUTE_TYPE,
    )
    model = WhisperModel(WHISPER_MODEL, device=WHISPER_DEVICE, compute_type=WHISPER_COMPUTE_TYPE)
    logger.info("AI Worker 启动完成, queue=%s, esIndex=%s", QUEUE_KEY, ES_INDEX)

    while True:
        item = redis_client.brpop(QUEUE_KEY, timeout=REDIS_BLOCK_SECONDS)
        if not item:
            continue
        _queue, task_json = item
        try:
            handle_task(model, es_client, task_json)
        except Exception:
            # 第一版先不做死信队列，避免坏任务阻塞队列；日志里保留完整任务方便人工重投。
            logger.exception("字幕向量化任务失败, task=%s", task_json)


if __name__ == "__main__":
    main()
