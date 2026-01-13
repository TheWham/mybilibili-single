package com.easylive.component;

import com.easylive.entity.constants.Constants;
import com.easylive.entity.dto.TokenUserInfoDTO;
import com.easylive.exception.BusinessException;
import com.easylive.redis.RedisUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.UUID;

import static com.easylive.entity.constants.Constants.REDIS_EXPIRE_TIME_DAY_COUNT;

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

    public void cleanWebToken(String tokenId) {
        // 清除web端token信息
        String webTokenKey = Constants.REDIS_WEB_TOKEN_KEY + tokenId;
        redisUtils.delete(webTokenKey);
    }

    public TokenUserInfoDTO getTokenInfo(String tokenId) {
        String tokenKey = Constants.REDIS_WEB_TOKEN_KEY + tokenId;
        return (TokenUserInfoDTO) redisUtils.get(tokenKey);
    }
}
