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
}
