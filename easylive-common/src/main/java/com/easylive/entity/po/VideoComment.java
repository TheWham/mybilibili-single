package com.easylive.entity.po;

import com.easylive.enums.DateTimePatternEnum;
import com.easylive.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author amani
 * @date 2026/03/09
 * @description 评论
 */
public class VideoComment implements Serializable {
	/**
	 * @description 评论id
	 */
    private Integer commentId;

	/**
	 * @description 父级评论id
	 */
    private Integer pCommentId;

	/**
	 * @description 视频id
	 */
    private String videoId;

	/**
	 * @description 视频用户id
	 */
    private String videoUserId;

	/**
	 * @description 回复内容
	 */
    private String content;

	/**
	 * @description 图片路径
	 */
    private String imgPath;

	/**
	 * @description 用户id
	 */
    private String userId;

	/**
	 * @description 回复人id
	 */
    private String replyUserId;

	/**
	 * @description 0:未置顶, 1:置顶
	 */
    private Integer topType;

	/**
	 * @description 发布时间
	 */
	@JsonFormat (pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat (pattern = "yyyy-MM-dd HH:mm:ss")
    private Date postTime;

	/**
	 * @description 喜欢数量
	 */
    private Integer likeCount;

	/**
	 * @description 讨厌数量
	 */
    private Integer hateCount;

	public void setCommentId(Integer commentId) {
		this.commentId = commentId;
	}
	public Integer getCommentId() {
		return this.commentId;
	}
	public void setPCommentId(Integer pCommentId) {
		this.pCommentId = pCommentId;
	}
	public Integer getPCommentId() {
		return this.pCommentId;
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
	public void setContent(String content) {
		this.content = content;
	}
	public String getContent() {
		return this.content;
	}
	public void setImgPath(String imgPath) {
		this.imgPath = imgPath;
	}
	public String getImgPath() {
		return this.imgPath;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserId() {
		return this.userId;
	}
	public void setReplyUserId(String replyUserId) {
		this.replyUserId = replyUserId;
	}
	public String getReplyUserId() {
		return this.replyUserId;
	}
	public void setTopType(Integer topType) {
		this.topType = topType;
	}
	public Integer getTopType() {
		return this.topType;
	}
	public void setPostTime(Date postTime) {
		this.postTime = postTime;
	}
	public Date getPostTime() {
		return this.postTime;
	}
	public void setLikeCount(Integer likeCount) {
		this.likeCount = likeCount;
	}
	public Integer getLikeCount() {
		return this.likeCount;
	}
	public void setHateCount(Integer hateCount) {
		this.hateCount = hateCount;
	}
	public Integer getHateCount() {
		return this.hateCount;
	}

	@Override
	public String toString() {
		return "VideoComment{" +
				"commentId='" + commentId + 
				", pCommentId='" + pCommentId + '\'' + 
				", videoId='" + videoId + '\'' + 
				", videoUserId='" + videoUserId + '\'' + 
				", content='" + content + '\'' + 
				", imgPath='" + imgPath + '\'' + 
				", userId='" + userId + '\'' + 
				", replyUserId='" + replyUserId + '\'' + 
				", topType='" + topType + '\'' + 
				", postTime='" + DateUtils.format(postTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) + '\'' + 
				", likeCount='" + likeCount + '\'' + 
				", hateCount='" + hateCount + '\'' + 
				'}';
	}

}