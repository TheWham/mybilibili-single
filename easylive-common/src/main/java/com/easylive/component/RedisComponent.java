package com.easylive.component;

import com.alibaba.fastjson2.JSON;
import com.easylive.constants.Constants;
import com.easylive.entity.dto.SysSettingDTO;
import com.easylive.entity.dto.TokenUserInfoDTO;
import com.easylive.entity.dto.UploadingFileDTO;
import com.easylive.entity.po.CategoryInfo;
import com.easylive.entity.po.VideoInfoFilePost;
import com.easylive.exception.BusinessException;
import com.easylive.redis.RedisUtils;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
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
    }

    public void saveCategoryList2Redis(List<CategoryInfo> categoryList) {
        String categoryListKey = Constants.REDIS_ADMIN_CATEGORY_KEY;
        redisUtils.set(categoryListKey, categoryList);
    }

    public List<CategoryInfo> getCategoryList()
    {

        Object value = redisUtils.get(Constants.REDIS_ADMIN_CATEGORY_KEY);
        if (Objects.isNull(value)) {
            return Collections.emptyList();
        }
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty() || list.get(0) instanceof CategoryInfo) {
                @SuppressWarnings("unchecked")
                List<CategoryInfo> result = (List<CategoryInfo>) list;
                return result;
            }
        }
        return JSON.parseArray(JSON.toJSONString(value), CategoryInfo.class);
    }

    public void saveFileInfo(String userId, UploadingFileDTO uploadingFileDto) {
        String redisUploadFileKey = Constants.REDIS_WEB_UPLOADING_FILE_INFO_KEY + userId + uploadingFileDto.getUploadId();
        redisUtils.setex(redisUploadFileKey, uploadingFileDto, Constants.REDIS_EXPIRE_TIME_ONE_DAY);
    }
    public UploadingFileDTO getUploadFileInfo(String key)
    {
       return (UploadingFileDTO) redisUtils.get(key);
    }

    public SysSettingDTO getSysSetting()
    {
        Object sysSetting = redisUtils.get(Constants.REDIS_SYS_SETTING_KEY);
        if (sysSetting == null)
            sysSetting = new SysSettingDTO();
        return (SysSettingDTO) sysSetting;
    }

    public void delUploadVideoInfo(String userId, @NotEmpty String uploadId) {
        String key = Constants.REDIS_WEB_UPLOADING_FILE_INFO_KEY + userId + uploadId;
        redisUtils.delete(key);
    }

    public void addFileList2DelQueue(String videoId, List<String> filePathList) {
        String key = Constants.REDIS_WEB_ADD_DEL_QUEUE_KEY + videoId;
        redisUtils.lpushAll(key, filePathList, Constants.REDIS_EXPIRE_TIME_ONE_DAY*7);
    }

    public void addFileList2TransferQueue(List<VideoInfoFilePost> addList) {
        String key = Constants.REDIS_WEB_ADD_TRANSFER_QUEUE_KEY;
        redisUtils.lpushAll(key, addList, 0);
    }

    public VideoInfoFilePost getTransferVideoInfo4Queue() {
        String key = Constants.REDIS_WEB_ADD_TRANSFER_QUEUE_KEY;
        return (VideoInfoFilePost)redisUtils.rpop(key);
    }

    public List<String> getDelFilePathsQueue(String videoId) {
        String key = Constants.REDIS_WEB_ADD_DEL_QUEUE_KEY + videoId;
        List list = redisUtils.getQueueList(key);
        return list;
    }

    public void cleanDelFilePaths(String videoId){
        String key = Constants.REDIS_WEB_ADD_DEL_QUEUE_KEY + videoId;
        redisUtils.delete(key);
    }

    public Integer reportVideoPlayOnline(String fileId, String deviceId) {
        String videoUserOnlineKey = String.format(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_USER, fileId, deviceId);
        String playCountOnlineKey = String.format(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE, fileId);

        Integer count = null;
        if (!redisUtils.keyExists(videoUserOnlineKey))
        {
            redisUtils.setex(videoUserOnlineKey, fileId,Constants.REDIS_EXPIRE_TIME_ONE_SECOND * 8);
            count = redisUtils.incrementex(playCountOnlineKey, Constants.REDIS_EXPIRE_TIME_ONE_SECOND * 10).intValue();
            return count;
        }

        redisUtils.expire(videoUserOnlineKey, Constants.REDIS_EXPIRE_TIME_ONE_SECOND * 8);
        redisUtils.expire(playCountOnlineKey, Constants.REDIS_EXPIRE_TIME_ONE_SECOND * 10);
        count = (Integer)redisUtils.get(playCountOnlineKey);
        count = count == null ? 1 : count;
        return count;
    }
}
