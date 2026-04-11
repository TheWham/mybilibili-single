package com.easylive.component;

import com.alibaba.fastjson2.JSON;
import com.easylive.constants.Constants;
import com.easylive.entity.dto.SysSettingDTO;
import com.easylive.entity.dto.TokenUserInfoDTO;
import com.easylive.entity.dto.UploadingFileDTO;
import com.easylive.entity.dto.UserActionSyncDTO;
import com.easylive.entity.po.CategoryInfo;
import com.easylive.entity.po.VideoInfoFilePost;
import com.easylive.entity.vo.UserInfoVO;
import com.easylive.exception.BusinessException;
import com.easylive.redis.RedisUtils;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.easylive.constants.Constants.REDIS_EXPIRE_TIME_DAY_COUNT;
import static com.easylive.constants.Constants.REDIS_EXPIRE_TIME_MINUTE_COUNT;

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

    public void updateTokenUserInfo(TokenUserInfoDTO tokenUserInfoDTO) {
        redisUtils.setex(Constants.REDIS_WEB_TOKEN_KEY + tokenUserInfoDTO.getTokenId(), tokenUserInfoDTO, Constants.REDIS_EXPIRE_TIME_ONE_DAY * REDIS_EXPIRE_TIME_DAY_COUNT);
    }

    public UserInfoVO getUserInfoVOInRedis(String userId) {
        Object o = redisUtils.get(Constants.REDIS_WEB_USER_INFO_KEY + userId);
        if (o == null)
            return null;
        return (UserInfoVO) o;
    }

    public void saveUserInfoVOInRedis(UserInfoVO userInfoVO) {
        // 存放登录信息
        redisUtils.setex(Constants.REDIS_WEB_USER_INFO_KEY + userInfoVO.getUserId(), userInfoVO, Constants.REDIS_EXPIRE_TIME_ONE_MINUTE * REDIS_EXPIRE_TIME_MINUTE_COUNT * 6);
    }

    public void delUserInfoInRedis(String userId) {
        redisUtils.delete(Constants.REDIS_WEB_USER_INFO_KEY + userId);
    }

    public void saveUserStatsInfo(String userId, Map<String, Integer> userStatsInfo, String date) {
        Map<String, Object> statsMap = new HashMap<>(userStatsInfo);
        redisUtils.hmset(Constants.REDIS_WEB_USER_STATS_KEY + date + ":" + userId, statsMap, Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.LENGTH_2);
    }

    //设置过期时间为两天避免昨天数据过期
    public void flashUserStatsExpire(String userId, String date) {
        redisUtils.expire(Constants.REDIS_WEB_USER_STATS_KEY + date + ":" + userId, Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.LENGTH_2);
    }

    public HashMap<String, Integer> getUserStatsInfo(String userId, String date) {
        return (HashMap<String, Integer>) redisUtils.hmget(Constants.REDIS_WEB_USER_STATS_KEY + date + ":" + userId);
    }

    public void saveVideoActionStatus(String userId, String videoId, Integer actionType, Integer actionCount) {
        redisUtils.set(buildVideoActionStatusKey(userId, videoId, actionType), actionCount);
    }

    public boolean hasVideoActionStatus(String userId, String videoId, Integer actionType) {
        return redisUtils.keyExists(buildVideoActionStatusKey(userId, videoId, actionType));
    }

    public void removeVideoActionStatus(String userId, String videoId, Integer actionType) {
        redisUtils.delete(buildVideoActionStatusKey(userId, videoId, actionType));
    }

    public void saveCommentActionStatus(String userId, Integer commentId, Integer actionType) {
        redisUtils.set(buildCommentActionStatusKey(userId, commentId), actionType);
    }

    public Integer getCommentActionStatus(String userId, Integer commentId) {
        Object value = redisUtils.get(buildCommentActionStatusKey(userId, commentId));
        if (value == null) {
            return null;
        }
        return Integer.parseInt(value.toString());
    }

    public void removeCommentActionStatus(String userId, Integer commentId) {
        redisUtils.delete(buildCommentActionStatusKey(userId, commentId));
    }

    public void addUserActionQueue(String queueKey, UserActionSyncDTO actionSyncDTO) {
        redisUtils.lpush(queueKey, actionSyncDTO, 0L);
    }

    public UserActionSyncDTO getNextUserActionQueue(String queueKey) {
        return (UserActionSyncDTO) redisUtils.rpop(queueKey);
    }

    public Long incrementUserStats(String userId, String field, long count) {
        String key = Constants.REDIS_WEB_USER_STATS_KEY + userId;
        Long value = redisUtils.hincr(key, field, count);
        redisUtils.expire(key, Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_USER_STATS_CACHE_TTL_DAYS);
        return value;
    }

    public Integer getUserStatsValue(String userId, String field) {
        Object value = redisUtils.hget(Constants.REDIS_WEB_USER_STATS_KEY + userId, field);
        if (value == null) {
            return null;
        }
        return Integer.parseInt(value.toString());
    }

    public void setUserStatsValue(String userId, String field, Integer value) {
        Map<String, Object> statsMap = new HashMap<>();
        statsMap.put(field, value);
        redisUtils.hmset(Constants.REDIS_WEB_USER_STATS_KEY + userId, statsMap, Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_USER_STATS_CACHE_TTL_DAYS);
    }

    public void saveRealtimeUserStatsInfo(String userId, Map<String, Integer> userStatsInfo) {
        Map<String, Object> statsMap = new HashMap<>(userStatsInfo);
        redisUtils.hmset(Constants.REDIS_WEB_USER_STATS_KEY + userId, statsMap, Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_USER_STATS_CACHE_TTL_DAYS);
    }

    public HashMap<String, Integer> getRealtimeUserStatsInfo(String userId) {
        return (HashMap<String, Integer>) redisUtils.hmget(Constants.REDIS_WEB_USER_STATS_KEY + userId);
    }

    public void refreshRealtimeUserStatsExpire(String userId) {
        redisUtils.expire(Constants.REDIS_WEB_USER_STATS_KEY + userId, Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_USER_STATS_CACHE_TTL_DAYS);
    }

    public void saveUserStatsSnapshot(String userId, String statsDay, Map<String, Integer> userStatsInfo) {
        Map<String, Object> statsMap = new HashMap<>(userStatsInfo);
        redisUtils.hmset(Constants.REDIS_WEB_USER_STATS_SNAPSHOT_KEY + statsDay + ":" + userId, statsMap, Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.LENGTH_2);
    }

    public HashMap<String, Integer> getUserStatsSnapshot(String userId, String statsDay) {
        return (HashMap<String, Integer>) redisUtils.hmget(Constants.REDIS_WEB_USER_STATS_SNAPSHOT_KEY + statsDay + ":" + userId);
    }

    public Set<String> getUserStatsSnapshotKeys(String statsDay) {
        return redisUtils.getByKeyPrefix(Constants.REDIS_WEB_USER_STATS_SNAPSHOT_KEY + statsDay + ":");
    }

    private String buildVideoActionStatusKey(String userId, String videoId, Integer actionType) {
        return Constants.REDIS_WEB_ACTION_VIDEO_STATUS_KEY + userId + ":" + videoId + ":" + actionType;
    }

    private String buildCommentActionStatusKey(String userId, Integer commentId) {
        return Constants.REDIS_WEB_ACTION_COMMENT_STATUS_KEY + userId + ":" + commentId;
    }

    public void saveKeyword(String keyword)
    {
        redisUtils.zaddCount(Constants.REDIS_KEY_VIDEO_SEARCH_COUNT, keyword);
    }

    public List<String> getSearchKeywordTop(Integer top) {
        return redisUtils.getZSetList(Constants.REDIS_KEY_VIDEO_SEARCH_COUNT, top - 1);
    }

    /**
     * 保存播放历史记录
     * @param videoId 视频id
     * @param userId 观看用户id
     * @param fileIndex 视频批号
     */
    public void saveVideoHistory(String videoId, String userId, Integer fileIndex) {
        //  获取绝对时间戳 保证排序正确
        long timestamp = System.currentTimeMillis();


        String key = Constants.REDIS_KEY_VIDEO_PLAY_HISTORY + userId;


        String member = videoId + ":" + (fileIndex == null ? 1 : fileIndex);

        // --- 开始 Redis 原子操作 (建议封装进 redisUtils) ---

        // 写入ZSet（Score 是时间戳，保证了最新看的永远最大）
        redisUtils.zaddCount4VideoHistory(key, member, (double) timestamp);

        //只保留最新的 1000 条，删掉排名靠后的旧数据
        // 索引 0 到 -1001 之间的全部删掉（即保留最后 1000 个）
    //    redisUtils.zremRangeByRank(key, 0, -1001);

        //设置 30 天过期（TTL）
        redisUtils.expire(key, Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.LENGTH_30);

        // 把该 userId 放入一个待同步 Set 集合中
        redisUtils.zaddUserId(Constants.REDIS_KEY_DIRTY_HISTORY_USER, userId);
    }

    /**
     * 保存播放量到hll
     * @param videoId 视频id
     * @param userId 观看用户id
     */
    public boolean saveVideoPlayCount2HLL(String videoId, String userId)
    {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = Constants.REDIS_KEY_VIDEO_PLAY_COUNT + dateStr + ":" + videoId;
        Long addCount = redisUtils.saveVideoPlayCount2HLL(key, userId, Constants.REDIS_EXPIRE_TIME_ONE_DAY * 2);
        return addCount != null && addCount > 0;
    }

    public boolean saveVideoEffectivePlay(String videoId, String userId) {
        String key = Constants.REDIS_KEY_VIDEO_PLAY_EFFECTIVE + videoId + ":" + userId;
        long expireTime = Constants.REDIS_EXPIRE_TIME_ONE_MINUTE * Constants.REDIS_VIDEO_PLAY_EFFECTIVE_EXPIRE_MINUTES;
        return redisUtils.setIfAbsent(key, videoId, expireTime);
    }

    public void addVideoPlayCountDelta(String videoId) {
        redisUtils.hincr(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_DELTA, videoId, 1);
        redisUtils.expire(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_DELTA, Constants.REDIS_EXPIRE_TIME_TWO_DAY);
    }

    public Map<String, Integer> getAllVideoPlayCountDelta() {
        Map<Object, Object> valueMap = redisUtils.hmget(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_DELTA);
        if (valueMap == null || valueMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Integer> resultMap = new HashMap<>(valueMap.size());
        for (Map.Entry<Object, Object> entry : valueMap.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            resultMap.put(entry.getKey().toString(), Integer.parseInt(entry.getValue().toString()));
        }
        return resultMap;
    }

    public void clearVideoPlayCountDelta(List<String> videoIds) {
        if (videoIds == null || videoIds.isEmpty()) {
            return;
        }
        redisUtils.hdel(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_DELTA, videoIds.toArray());
    }

    public Set<String> getDirtyHistoryUsers() {
        return redisUtils.getSetMembers(Constants.REDIS_KEY_DIRTY_HISTORY_USER);
    }

    public List<String> getVideoHistoryList(String userId) {
        return redisUtils.getZSetList(Constants.REDIS_KEY_VIDEO_PLAY_HISTORY + userId, -1);
    }

}
