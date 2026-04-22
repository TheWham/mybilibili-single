package com.easylive.aspect;

import com.easylive.annotation.MessageInterceptor;
import com.easylive.component.RedisComponent;
import com.easylive.constants.Constants;
import com.easylive.entity.po.UserAction;
import com.easylive.entity.po.UserMessage;
import com.easylive.entity.po.VideoComment;
import com.easylive.entity.po.VideoInfoPost;
import com.easylive.entity.query.VideoCommentQuery;
import com.easylive.entity.query.VideoInfoPostQuery;
import com.easylive.enums.MessageTypeEnum;
import com.easylive.enums.UserActionTypeEnum;
import com.easylive.enums.VideoStatusEnum;
import com.easylive.mappers.VideoCommentMapper;
import com.easylive.mappers.VideoInfoPostMapper;
import com.easylive.utils.JsonUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.easylive.constants.Constants.AUDIT_VIDEO_NAME;


@Aspect
@Component
public class UserMessageAspect extends GlobalOperationAspect{

    @Resource
    private RedisComponent redisComponent;
    @Resource
    private VideoCommentMapper<VideoComment, VideoCommentQuery> videoCommentMapper;
    @Resource
    private VideoInfoPostMapper<VideoInfoPost, VideoInfoPostQuery> videoInfoPostMapper;


    @Around("@annotation(com.easylive.annotation.MessageInterceptor) || @within(com.easylive.annotation.MessageInterceptor)")
    public Object isSendMessage(ProceedingJoinPoint joinPoint) throws Throwable
    {
        MessageInterceptor annotation = getAnnotation(joinPoint, MessageInterceptor.class);
        if (annotation == null || !annotation.sendMessage())
            return joinPoint.proceed();

        Object result = joinPoint.proceed();
        sendMessage(joinPoint, annotation);
        return result;
    }

    private void sendMessage(JoinPoint joinPoint, MessageInterceptor annotation)
    {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof UserAction userAction) {
                saveUserActionMessage(userAction, annotation);
                return;
            }
            if (arg instanceof VideoComment videoComment) {
                saveCommentMessage(videoComment, annotation.messageType());
                return;
            }
        }
        // 视频审核这类后台动作没有统一的参数对象，只能按方法语义补一层识别。
        if (AUDIT_VIDEO_NAME.equals(joinPoint.getSignature().getName())) {
            saveAuditVideoMessage(args, annotation.messageType());
        }
    }

    private void saveUserActionMessage(UserAction userAction, MessageInterceptor annotation) {
        if (userAction == null || userAction.getActionType() == null) {
            return;
        }

        UserActionTypeEnum actionTypeEnum = UserActionTypeEnum.getEnum(userAction.getActionType());
        if (actionTypeEnum == null) {
            return;
        }

        if (!annotation.resolveByActionType()) {
            addMessage(userAction.getVideoUserId(), userAction.getUserId(), userAction.getVideoId(), annotation.messageType(), buildUserActionExtendJson(userAction));
            return;
        }

        switch (actionTypeEnum) {
            case VIDEO_LIKE:
                // 这里不能只判断 key 是否存在。
                // 取消点赞时 Redis 会把状态写成 0 并保留一小段时间，如果只看 key 存不存在，取消动作也会被误判成“还在点赞”，从而重复发消息。
                Integer likeStatus = redisComponent.getVideoActionStatus(userAction.getUserId(), userAction.getVideoId(), userAction.getActionType());
                if (likeStatus != null && likeStatus > 0) {
                    addMessage(userAction.getVideoUserId(), userAction.getUserId(), userAction.getVideoId(), MessageTypeEnum.LIKE, buildUserActionExtendJson(userAction));
                }
                break;  
            case VIDEO_COLLECT:
                Integer collectStatus = redisComponent.getVideoActionStatus(userAction.getUserId(), userAction.getVideoId(), userAction.getActionType());
                if (collectStatus != null && collectStatus > 0) {
                    addMessage(userAction.getVideoUserId(), userAction.getUserId(), userAction.getVideoId(), MessageTypeEnum.COLLECT, buildUserActionExtendJson(userAction));
                }
                break;
            case COMMENT_LIKE:
                Integer actionStatus = redisComponent.getCommentActionStatus(userAction.getUserId(), userAction.getCommentId());
                if (actionStatus != null && actionStatus.equals(UserActionTypeEnum.COMMENT_LIKE.getType())) {
                    VideoComment videoComment = videoCommentMapper.selectByCommentId(userAction.getCommentId());
                    if (videoComment != null) {
                        addMessage(videoComment.getUserId(), userAction.getUserId(), videoComment.getVideoId(), MessageTypeEnum.LIKE, buildCommentActionExtendJson(userAction, videoComment));
                    }
                }
                break;
            default:
                break;
        }
    }

    private void saveCommentMessage(VideoComment videoComment, MessageTypeEnum messageTypeEnum) {
        if (videoComment == null) {
            return;
        }
        //需要处理细节 回复接收人不同
        Boolean isReply = StringUtils.isNotBlank(videoComment.getReplyUserId());
        String receiveUserId =  isReply ? videoComment.getReplyUserId() : videoComment.getVideoUserId();
        addMessage(receiveUserId, videoComment.getUserId(), videoComment.getVideoId(), messageTypeEnum, buildCommentExtendJson(videoComment, isReply));
    }

    private void addMessage(String receiveUserId, String sendUserId, String videoId, MessageTypeEnum messageTypeEnum, String extendJson) {
        // 系统消息本身没有发送人，因此这里只强制校验接收人，
        // 普通互动消息再额外拦掉“自己给自己发通知”的场景。
        if (StringUtils.isBlank(receiveUserId)) {
            return;
        }
        if (StringUtils.isNotBlank(sendUserId) && receiveUserId.equals(sendUserId)) {
            return;
        }

        UserMessage userMessage = new UserMessage();
        userMessage.setUserId(receiveUserId);
        userMessage.setSendUserId(sendUserId);
        userMessage.setVideoId(videoId);
        userMessage.setMessageType(messageTypeEnum.getType());
        userMessage.setReadType(Constants.ZERO);
        userMessage.setCreateTime(new Date());
        userMessage.setExtendJson(extendJson);
        // 通知不用卡在主业务事务里，先扔队列，后面批量落库。
        redisComponent.addUserMessageQueue(userMessage);
    }

    private void saveAuditVideoMessage(Object[] args, MessageTypeEnum messageTypeEnum) {
        if (args == null || args.length < 2) {
            return;
        }

        String videoId = args[0] instanceof String ? (String) args[0] : null;
        Integer auditStatus = args[1] instanceof Integer ? (Integer) args[1] : null;
        String reason = args.length > 2 && args[2] instanceof String ? (String) args[2] : null;
        if (StringUtils.isBlank(videoId) || auditStatus == null) {
            return;
        }

        if (auditStatus.equals(VideoStatusEnum.STATUS_3.getStatus()) && auditStatus.equals(VideoStatusEnum.STATUS_4.getStatus())) {
            return;
        }

        VideoInfoPost videoInfoPost = videoInfoPostMapper.selectByVideoId(videoId);
        if (videoInfoPost == null || StringUtils.isBlank(videoInfoPost.getUserId())) {
            return;
        }

        addMessage(
                videoInfoPost.getUserId(),
                null,
                videoId,
                messageTypeEnum,
                buildAuditVideoExtendJson(videoInfoPost, auditStatus, reason)
        );
    }

    private String buildUserActionExtendJson(UserAction userAction) {
        Map<String, Object> extendInfo = new HashMap<>();
        extendInfo.put("actionType", userAction.getActionType());
        extendInfo.put("actionCount", userAction.getActionCount());
        extendInfo.put("videoId", userAction.getVideoId());
        return JsonUtils.convertObj2Json(extendInfo);
    }

    private String buildCommentActionExtendJson(UserAction userAction, VideoComment videoComment) {
        Map<String, Object> extendInfo = new HashMap<>();
        extendInfo.put("actionType", userAction.getActionType());
        extendInfo.put("commentId", userAction.getCommentId());
        extendInfo.put("videoId", videoComment.getVideoId());
        extendInfo.put("content", videoComment.getContent());
        return JsonUtils.convertObj2Json(extendInfo);
    }

    private String buildCommentExtendJson(VideoComment videoComment, Boolean isReply) {
        Map<String, Object> extendInfo = new HashMap<>();
        extendInfo.put("commentId", videoComment.getCommentId());
        extendInfo.put("replyCommentId", videoComment.getReplyCommentId());
        extendInfo.put("pCommentId", videoComment.getPCommentId());
        String content = isReply ? "messageContent" : "messageContentReply";
        extendInfo.put(content, videoComment.getContent());
        extendInfo.put("replyUserId", videoComment.getReplyUserId());
        return JsonUtils.convertObj2Json(extendInfo);
    }

    private String buildAuditVideoExtendJson(VideoInfoPost videoInfoPost, Integer auditStatus, String reason) {
        Map<String, Object> extendInfo = new HashMap<>();
        extendInfo.put("videoId", videoInfoPost.getVideoId());
        extendInfo.put("videoName", videoInfoPost.getVideoName());
        extendInfo.put("videoCover", videoInfoPost.getVideoCover());
        extendInfo.put("auditStatus", auditStatus);
        // 前端失败态直接读取 messageContent 展示失败原因，这里字段名和现有模板保持一致。
        extendInfo.put("messageContent", StringUtils.defaultString(reason));
        return JsonUtils.convertObj2Json(extendInfo);
    }
}
