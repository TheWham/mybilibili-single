package com.easylive.service;

import com.easylive.entity.po.CategoryInfo;
import com.easylive.entity.query.CategoryInfoQuery;
import com.easylive.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * @author amani
 * @date 2026/01/19
 * @description 分类信息
Service
 */
public interface CategoryInfoService {

	/**
	 * @description 根据条件查询
	 */
	List<CategoryInfo> findListByParam(CategoryInfoQuery param);

	/**
	 * @description 根据条件查询数量
	 */
	Integer findCountByParam(CategoryInfoQuery param);

	/**
	 * @description 分页查询
	 */
	PaginationResultVO<CategoryInfo> findListByPage(CategoryInfoQuery param);

	/**
	 * @description 新增
	 */
	Integer add(CategoryInfo bean);

	/**
	 * @description 批量新增
	 */
	Integer addBatch(List<CategoryInfo>  listBean);

	/**
	 * @description 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<CategoryInfo> listBean);


	/**
	 * @description 根据 CategoryId查询
	 */
	CategoryInfo getCategoryInfoByCategoryId(String categoryId);

	/**
	 * @description 根据 CategoryId更新
	 */
	Integer updateCategoryInfoByCategoryId(CategoryInfo bean, String categoryId);

	/**
	 * @description 根据 CategoryId删除
	 */
	Integer deleteCategoryInfoByCategoryId(String categoryId);


	/**
	 * @description 根据 CategoryCode查询
	 */
	CategoryInfo getCategoryInfoByCategoryCode(String categoryCode);

	/**
	 * @description 根据 CategoryCode更新
	 */
	Integer updateCategoryInfoByCategoryCode(CategoryInfo bean, String categoryCode);

	/**
	 * @description 根据 CategoryCode删除
	 */
	Integer deleteCategoryInfoByCategoryCode(String categoryCode);

}