package com.easylive.constants;


/**
 * @version 2026.1.6
 * @author Amani
 */
public class Constants {

    public final static Integer ZERO = 0;
    public final static Integer ONE = 1;
    public final static Integer USER_ID_LENGTH = 10;
    public final static Integer DEFAULT_COIN_COUNT = 100;
    public final static Integer TWO = 2;
    public final static String REDIS_PREFIX = "easylive:";
    public final static String REDIS_CHECK_CODE_KEY = REDIS_PREFIX + "checkCodeKey:";
    public final static Integer REDIS_EXPIRE_TIME_ONE_MINUTE = 60000;
    public final static Integer REDIS_EXPIRE_TIME_MINUTE_COUNT = 5;
    public final static String PASSWORD_REGEX = "^(?=.*\\d)(?=.*[a-zA-Z])[\\da-zA-Z~!@#$%^&*_]{8,18}$";
    public final static String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public static final String REDIS_WEB_TOKEN_KEY = REDIS_PREFIX + "web:redisToken:";
    public static final Integer REDIS_EXPIRE_TIME_ONE_DAY = REDIS_EXPIRE_TIME_ONE_MINUTE * 60 * 24;
    public static final Integer REDIS_EXPIRE_TIME_DAY_COUNT = 7;
    public static final Integer WEB_EXPIRE_TIME_DAY_COUNT = 7;
    public static final String WEB_TOKEN_KEY = "token";
    public static final Integer WEB_TOKEN_EXPIRE_TIME = REDIS_EXPIRE_TIME_ONE_DAY / 1000;
    public static final Integer ADMIN_TOKEN_EXPIRE_TIME = REDIS_EXPIRE_TIME_ONE_DAY / 1000;
    public static final String REDIS_ADMIN_TOKEN_KEY = REDIS_PREFIX + "admin:redisToken:";
    public static final String ADMIN_TOKEN_KEY = "adminToken";
    public static final String REDIS_USER_TOKEN_KEY = REDIS_PREFIX + "web:redisUserToken:";
    public static final String REDIS_ADMIN_CATEGORY_KEY = REDIS_PREFIX  + "admin:category:list:";


    public static final String FILE_PATH_FOLDER = "file/";
    public static final String FILE_PATH_FOLDER_VIDEO = "video/";
    public static final Integer LENGTH_30 = 30;
    public static final String FILE_PATH_FOLDER_TEMP = "temp/";
    public static final String FILE_PATH_FOLDER_COVER = "cover/";
    public static final String IMAGE_THUMBNAIL_SUFFIX = "_thumbnail";
    public static final Integer LENGTH_15 = 15;
    public static final Integer LENGTH_10 = 10;
    public static final String REDIS_WEB_UPLOADING_FILE_INFO_KEY = REDIS_PREFIX + "uploadFileInfo:";
    public static final Long MB_SIZE = 1024 * 1024L;
    public static final String REDIS_SYS_SETTING_KEY = REDIS_PREFIX + "sysSetting:";
    //消息队列

    public static final String REDIS_WEB_ADD_DEL_QUEUE_KEY = REDIS_PREFIX + "queue:del:file:list:";
    public static final Integer LENGTH_20 = 20;
    public static final String REDIS_WEB_ADD_TRANSFER_QUEUE_KEY = REDIS_PREFIX + "queue:transfer:file:list:";
    public static final Integer LENGTH_2 = 2;
    public static final String FILE_TEMP_MP4 = "/temp.mp4";
    public static final String VIDEO_CODEC_HEVC = "hevc";
    public static final String FILE_VIDEO_TEMP_SUFFIX = "_temp";
    public static final String TS_NAME = "index.ts";
    public static final String M3U8_NAME = "index.m3u8";

    public static final String REDIS_WEB_ACTION_VIDEO_COMMENT_QUEUE_KEY = REDIS_PREFIX + "queue:action:video:comment:list:";
    public static final String REDIS_WEB_ACTION_VIDEO_COIN_QUEUE_KEY = REDIS_PREFIX + "queue:action:video:coin:list:";
    public static final String REDIS_WEB_ACTION_VIDEO_COLLECT_QUEUE_KEY = REDIS_PREFIX + "queue:action:video:collect:list:";
    public static final String REDIS_WEB_ACTION_VIDEO_LIKE_QUEUE_KEY = REDIS_PREFIX + "queue:action:video:like:list:";
    public static final String REDIS_WEB_ACTION_VIDEO_STATUS_KEY = REDIS_PREFIX + "action:video:status:";
    public static final String REDIS_WEB_ACTION_COMMENT_STATUS_KEY = REDIS_PREFIX + "action:comment:status:";


    //在线播放人数

    public static final String REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE_PREIFX = REDIS_PREFIX + "video:play:online:";
    public static final String REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE = REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE_PREIFX + "count:%s";
    public static final String REDIS_KEY_VIDEO_PLAY_COUNT_USER_PREFIX = "user:";
    public static final String REDIS_KEY_VIDEO_PLAY_COUNT_USER = REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE_PREIFX + REDIS_KEY_VIDEO_PLAY_COUNT_USER_PREFIX + "%s:%s";
    public static final Integer REDIS_EXPIRE_TIME_ONE_SECOND = 1000;
    public static final Integer UPDATE_NAME_COIN = 3;

    //用户信息存储

    public static final String REDIS_WEB_USER_INFO_KEY = REDIS_PREFIX + "web:userInfo:showVO:";
    public static final String REDIS_WEB_USER_STATS_KEY = REDIS_PREFIX + "web:userInfo:stats:";
    public static final String REDIS_WEB_USER_STATS_SNAPSHOT_KEY = REDIS_PREFIX + "web:userInfo:stats:snapshot:";
    public static final Integer REDIS_USER_STATS_CACHE_TTL_DAYS = 15;
}

