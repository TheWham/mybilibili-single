package com.easylive.entity.po;

import java.io.Serializable;

/**
 * @author amani
 * @date 2026/01/19
 * @description 分类信息

 */
public class CategoryInfo implements Serializable {
	/**
	 * @description 自增分类id
	 */
    private String categoryId;

	/**
	 * @description 分类名称
	 */
    private String categoryName;

	/**
	 * @description 分类编码
	 */
    private String categoryCode;

	/**
	 * @description 父级分类id

	 */
    private String pCategoryId;

	/**
	 * @description 分类背景图
	 */
    private String background;

	/**
	 * @description 分类名称图标
	 */
    private String icon;

	/**
	 * @description 分类排列优先级
	 */
    private Integer sort;

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}
	public String getCategoryId() {
		return this.categoryId;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	public String getCategoryName() {
		return this.categoryName;
	}
	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}
	public String getCategoryCode() {
		return this.categoryCode;
	}
	public void setPCategoryId(String pCategoryId) {
		this.pCategoryId = pCategoryId;
	}
	public String getPCategoryId() {
		return this.pCategoryId;
	}
	public void setBackground(String background) {
		this.background = background;
	}
	public String getBackground() {
		return this.background;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getIcon() {
		return this.icon;
	}
	public void setSort(Integer sort) {
		this.sort = sort;
	}
	public Integer getSort() {
		return this.sort;
	}

	@Override
	public String toString() {
		return "CategoryInfo{" +
				"categoryId='" + categoryId + 
				", categoryName='" + categoryName + '\'' + 
				", categoryCode='" + categoryCode + '\'' + 
				", pCategoryId='" + pCategoryId + '\'' + 
				", background='" + background + '\'' + 
				", icon='" + icon + '\'' + 
				", sort='" + sort + '\'' + 
				'}';
	}

}