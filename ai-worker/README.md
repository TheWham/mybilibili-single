# Easylive AI Worker

Python Worker 负责消费 Java 投递的字幕向量化任务：

1. 从 Redis 队列 `easylive:queue:ai:subtitle-vector` 读取任务。
2. 用 `faster-whisper` 提取视频字幕。
3. 用本地 Ollama `bge-m3:567m` 生成字幕片段向量。
4. 批量写入 Elasticsearch 索引 `easylive_video_subtitle_vector`。
5. 写入成功后删除 Java 为字幕处理保留的 `temp.mp4`。

## 启动

```bash
cd ai-worker
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
python worker.py
```

## 环境变量

| 变量 | 默认值 | 说明 |
|---|---|---|
| `EASYLIVE_AI_QUEUE` | `easylive:queue:ai:subtitle-vector` | Redis 任务队列 |
| `EASYLIVE_REDIS_URL` | `redis://127.0.0.1:6379/0` | Redis 地址 |
| `EASYLIVE_ES_URL` | `http://127.0.0.1:9201` | Elasticsearch 地址 |
| `EASYLIVE_ES_INDEX` | `easylive_video_subtitle_vector` | 字幕向量索引 |
| `EASYLIVE_EMBEDDING_MODEL` | `bge-m3:567m` | Ollama 向量模型 |
| `EASYLIVE_WHISPER_MODEL` | `small` | Whisper 模型 |
| `EASYLIVE_WHISPER_DEVICE` | `cuda` | `cuda` 或 `cpu` |
| `EASYLIVE_WHISPER_COMPUTE_TYPE` | `float16` | GPU 常用 `float16`，CPU 可改 `int8` |
