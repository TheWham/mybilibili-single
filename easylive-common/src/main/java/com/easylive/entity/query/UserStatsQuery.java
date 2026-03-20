package com.easylive.entity.query;

/**
 * @author amani
 * @since 2026/03/21
 * 用户数量统计表
 */
public class UserStatsQuery extends BaseQuery {
	/**
	 * 用户id 关联user_info
	 */
	private String userId;
	private String userIdFuzzy;
	/**
	 * 播放数量
	 */
	private Integer playCount;

	/**
	 * 点赞数量
	 */
	private Integer likeCount;

	/**
	 * 当前硬币数量
	 */
	private Integer currentCoinCount;

	/**
	 * 关注数量
	 */
	private Integer focusCount;

	/**
	 * 粉丝数量
	 */
	private Integer fansCount;

	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserId() {
		return this.userId;
	}
	public void setPlayCount(Integer playCount) {
		this.playCount = playCount;
	}
	public Integer getPlayCount() {
		return this.playCount;
	}
	public void setLikeCount(Integer likeCount) {
		this.likeCount = likeCount;
	}
	public Integer getLikeCount() {
		return this.likeCount;
	}
	public void setCurrentCoinCount(Integer currentCoinCount) {
		this.currentCoinCount = currentCoinCount;
	}
	public Integer getCurrentCoinCount() {
		return this.currentCoinCount;
	}
	public void setFocusCount(Integer focusCount) {
		this.focusCount = focusCount;
	}
	public Integer getFocusCount() {
		return this.focusCount;
	}
	public void setFansCount(Integer fansCount) {
		this.fansCount = fansCount;
	}
	public Integer getFansCount() {
		return this.fansCount;
	}
	public void setUserIdFuzzy(String userIdFuzzy) {
		this.userIdFuzzy = userIdFuzzy;
	}
	public String getUserIdFuzzy() {
		return this.userIdFuzzy;
	}

}