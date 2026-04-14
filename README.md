# Easylive Docker 启动说明

## 说明

项目已经补好了 Docker 运行文件，支持一键启动下面这些服务：

- `mysql`
- `redis`
- `elasticsearch`
- `easylive-web`
- `easylive-admin`

其中数据库初始化脚本已经挂到 Docker 启动流程里：

- [01-easylive.sql](docker/mysql/init/01-easylive.sql)

MySQL 容器第一次初始化时会自动执行这个脚本。

## 启动前准备

1. 机器需要先装好 Docker 和 Docker Compose。
2. 确认下面端口没有被占用：
   - `3306`
   - `6379`
   - `7070`
   - `7071`
   - `9201`
3. 第一次启动前，如果你想重新初始化数据库，先删除旧数据目录：

```powershell
Remove-Item -Recurse -Force .\docker-data\mysql
```

这一步会清空 MySQL 数据，只在你确定要重建库的时候再做。

## 启动命令

在项目根目录执行：

```powershell
docker compose up --build -d
```

如果只想先看前台服务日志，可以执行：

```powershell
docker compose logs -f easylive-web
```

如果想看全部服务日志：

```powershell
docker compose logs -f
```

## 访问地址

启动完成后可以访问：

- 前台服务：`http://localhost:7071/web`
- 后台服务：`http://localhost:7070/admin`
- Elasticsearch：`http://localhost:9201`

## 容器里的默认配置

Docker 环境下应用会自动读取 `application-docker.yml`：

- Web 配置：
  - [application-docker.yml](easylive-java/easylive-web/src/main/resources/application-docker.yml)
- Admin 配置：
  - [application-docker.yml](easylive-java/easylive-admin/src/main/resources/application-docker.yml)

默认连接信息如下：

- MySQL
  - host: `mysql`
  - database: `easylive`
  - username: `root`
  - password: `root123456`
- Redis
  - host: `redis`
  - port: `6379`
- Elasticsearch
  - host: `elasticsearch`
  - port: `9200`

## 数据和文件挂载目录

本地挂载目录如下：

- MySQL 数据：`./docker-data/mysql`
- Redis 数据：`./docker-data/redis`
- Elasticsearch 数据：`./docker-data/elasticsearch`
- 项目文件目录：`./docker-data/app`

项目里的：

- 视频文件
- 封面
- 转码切片

都会走容器内的 `/app/data/`，对应到本地就是 `./docker-data/app`。

## 常用命令

停止服务：

```powershell
docker compose down
```

停止并删除数据卷映射出来的数据目录不会自动清空，如果你要彻底重建，手动删 `docker-data` 目录即可。

重新构建某个服务：

```powershell
docker compose build easylive-web
docker compose up -d easylive-web
```

进入 MySQL 容器：

```powershell
docker exec -it easylive-mysql mysql -uroot -proot123456 easylive
```

进入 Redis 容器：

```powershell
docker exec -it easylive-redis redis-cli
```

## 注意事项

1. MySQL 初始化脚本只会在容器第一次建库时执行。
2. 如果 `docker-data/mysql` 已经存在旧数据，修改 SQL 后直接重启容器不会重新导入。
3. 项目依赖 `ffmpeg`，镜像里已经安装了 Linux 版 `ffmpeg`，容器内可以直接使用。
