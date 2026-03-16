package com.easylive.entity.po;

import com.easylive.enums.DateTimePatternEnum;
import com.easylive.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author amani
 * @since 2026/03/16
 * 用户评论行为
 */
public class UserCommentAction implements Serializable {
	/**
	 * 自增id
	 */
    private Integer actionId;

	/**
	 * 视频Id
	 */
    private String videoId;

	/**
	 * 视频用户id
	 */
    private String videoUserId;

	/**
	 * 评论id
	 */
    private Integer commentId;

	/**
	 * 0:评论喜欢点赞 1:讨厌评论
	 */
    private Integer actionType;

	/**
	 * 用户id
	 */
    private String userId;

	/**
	 * 操作时间
	 */
	@JsonFormat (pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat (pattern = "yyyy-MM-dd HH:mm:ss")
    private Date actionTime;

	public void setActionId(Integer actionId) {
		this.actionId = actionId;
	}
	public Integer getActionId() {
		return this.actionId;
	}
	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}
	public String getVideoId() {
		return this.videoId;
	}
	public void setVideoUserId(String videoUserId) {
		this.videoUserId = videoUserId;
	}
	public String getVideoUserId() {
		return this.videoUserId;
	}
	public void setCommentId(Integer commentId) {
		this.commentId = commentId;
	}
	public Integer getCommentId() {
		return this.commentId;
	}
	public void setActionType(Integer actionType) {
		this.actionType = actionType;
	}
	public Integer getActionType() {
		return this.actionType;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserId() {
		return this.userId;
	}
	public void setActionTime(Date actionTime) {
		this.actionTime = actionTime;
	}
	public Date getActionTime() {
		return this.actionTime;
	}

	@Override
	public String toString() {
		return "UserCommentAction{" +
				"actionId='" + actionId + 
				", videoId='" + videoId + '\'' + 
				", videoUserId='" + videoUserId + '\'' + 
				", commentId='" + commentId + '\'' + 
				", actionType='" + actionType + '\'' + 
				", userId='" + userId + '\'' + 
				", actionTime='" + DateUtils.format(actionTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) + '\'' + 
				'}';
	}

}