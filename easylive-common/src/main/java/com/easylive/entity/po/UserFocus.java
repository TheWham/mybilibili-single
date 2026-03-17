package com.easylive.entity.po;

import com.easylive.enums.DateTimePatternEnum;
import com.easylive.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author amani
 * @since 2026/03/18
 * 用户关注列表
 */
public class UserFocus implements Serializable {
	/**
	 * 用户id
	 */
    private String userId;

	/**
	 * 关注用户id
	 */
    private String userFocusId;

	/**
	 * 关注时间
	 */
	@JsonFormat (pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat (pattern = "yyyy-MM-dd HH:mm:ss")
    private Date focusTime;

	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserId() {
		return this.userId;
	}
	public void setUserFocusId(String userFocusId) {
		this.userFocusId = userFocusId;
	}
	public String getUserFocusId() {
		return this.userFocusId;
	}
	public void setFocusTime(Date focusTime) {
		this.focusTime = focusTime;
	}
	public Date getFocusTime() {
		return this.focusTime;
	}

	@Override
	public String toString() {
		return "UserFocus{" +
				"userId='" + userId + 
				", userFocusId='" + userFocusId + '\'' + 
				", focusTime='" + DateUtils.format(focusTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) + '\'' + 
				'}';
	}

}