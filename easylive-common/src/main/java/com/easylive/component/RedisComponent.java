package com.easylive.component;

import com.easylive.constants.Constants;
import com.easylive.entity.dto.TokenUserInfoDTO;
import com.easylive.exception.BusinessException;
import com.easylive.redis.RedisUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.UUID;

import static com.easylive.constants.Constants.REDIS_EXPIRE_TIME_DAY_COUNT;

@Component("redisComponent")
public class RedisComponent {

    @Resource
    public RedisUtils redisUtils;


    public String saveCode(String code){
        String checkCodeKey = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_CHECK_CODE_KEY + checkCodeKey, code, Constants.REDIS_EXPIRE_TIME_ONE_MINUTE * Constants.REDIS_EXPIRE_TIME_MINUTE_COUNT);
        return checkCodeKey;
    }

    public String getCode(String checkCodeKey)
    {
        String aCheckCodeKey = Constants.REDIS_CHECK_CODE_KEY + checkCodeKey;
        Object ans = redisUtils.get(aCheckCodeKey);
        if (Objects.isNull(ans))
            throw new BusinessException("验证码失效");
        return ans.toString();
    }

    public void cleanCheckCode(String checkCodeKey)
    {
        if (Objects.isNull(this.getCode(checkCodeKey)))
            return;
        redisUtils.delete(checkCodeKey);
    }

    public void saveTokenUserInfo(TokenUserInfoDTO tokenUserInfoDTO) {
        // 存放登录信息
        String tokenId = UUID.randomUUID().toString();
        tokenUserInfoDTO.setExpireAt(System.currentTimeMillis() + Constants.REDIS_EXPIRE_TIME_ONE_DAY * REDIS_EXPIRE_TIME_DAY_COUNT);
        tokenUserInfoDTO.setTokenId(tokenId);
        redisUtils.setex(Constants.REDIS_WEB_TOKEN_KEY + tokenId, tokenUserInfoDTO, Constants.REDIS_EXPIRE_TIME_ONE_DAY * REDIS_EXPIRE_TIME_DAY_COUNT);
    }

    public String saveToken4Admin(String account) {
        // 存放登录信息
        String tokenId = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_ADMIN_TOKEN_KEY + tokenId, account, Constants.REDIS_EXPIRE_TIME_ONE_DAY);
        return tokenId;
    }


    public void cleanWebToken(String tokenId) {
        // 清除web端token信息
        String webTokenKey = Constants.REDIS_WEB_TOKEN_KEY + tokenId;
        redisUtils.delete(webTokenKey);
    }

    public void cleanAdminToken(String tokenId) {
        // 清除admin端token信息
        String webTokenKey = Constants.REDIS_ADMIN_TOKEN_KEY + tokenId;
        redisUtils.delete(webTokenKey);
    }

    public TokenUserInfoDTO getTokenInfo(String tokenId) {
        String tokenKey = Constants.REDIS_WEB_TOKEN_KEY + tokenId;
        return (TokenUserInfoDTO) redisUtils.get(tokenKey);
    }

    //清除已经登录token实现单点登录
    public void cleanExistToken(String userId)
    {
        String userTokenKey = Constants.REDIS_USER_TOKEN_KEY + userId;
        redisUtils.delete(userTokenKey);
    }

    public String getTokenIdByUserId(String userId)
    {
        String userTokenKey = Constants.REDIS_USER_TOKEN_KEY + userId;
        Object value = redisUtils.get(userTokenKey);
        if (Objects.isNull(value))
            return null;
        return value.toString();
    }

    public void saveTokenIdByUserId(String userId, String token) {
        // 通过userId存放token
        redisUtils.setex(Constants.REDIS_USER_TOKEN_KEY + userId, token, Constants.REDIS_EXPIRE_TIME_ONE_DAY * REDIS_EXPIRE_TIME_DAY_COUNT);
    }

    public Object getTokenInfo4Admin(String tokenId) {
        String tokenKey = Constants.REDIS_ADMIN_TOKEN_KEY + tokenId;
        return redisUtils.get(tokenKey);
    }}
