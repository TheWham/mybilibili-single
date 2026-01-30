package com.easylive.web.controller;

import com.easylive.entity.po.CategoryInfo;
import com.easylive.entity.query.CategoryInfoQuery;
import com.easylive.entity.query.SimplePage;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.enums.PageSize;
import com.easylive.service.CategoryInfoService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
/**
 * @author amani
 * @date 2026/01/19
 * @description 分类信息
Service
 */

@RestController
@RequestMapping("categoryInfo")
public class CategoryInfoController extends ABaseController {
	@Resource
	private CategoryInfoService categoryInfoService;

	/**
	 * @description 根据条件分页查询
	 */
	@RequestMapping("loadDataList")
	public ResponseVO loadDataList (CategoryInfoQuery query) {
		return getSuccessResponseVO(categoryInfoService.findListByPage(query));
	}

	/**
	 * @description 根据条件查询数量
	 */
	@RequestMapping("findCountByParam")
	public Integer findCountByParam(CategoryInfoQuery param) {
		return this.categoryInfoService.findCountByParam(param);
	}

	/**
	 * @description 分页查询
	 */
	@RequestMapping("findListByPage")
	public PaginationResultVO<CategoryInfo> findListByPage(CategoryInfoQuery param) {
		Integer count = this.categoryInfoService.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<CategoryInfo> list = this.categoryInfoService.findListByParam(param);
		PaginationResultVO<CategoryInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * @description 新增
	 */
	@RequestMapping("add")
	public ResponseVO add(CategoryInfo bean) {
		categoryInfoService.add(bean);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 批量新增
	 */
	@RequestMapping("addBatch")
	public ResponseVO addBatch(@RequestBody List<CategoryInfo> listBean) {
		categoryInfoService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 批量新增/修改
	 */
	@RequestMapping("addOrUpdateBatch")
	public ResponseVO addOrUpdateBatch(@RequestBody List<CategoryInfo> listBean) {
		categoryInfoService.addOrUpdateBatch(listBean);
		return getSuccessResponseVO(null);
	}


	/**
	 * @description 根据 CategoryId查询
	 */
	@RequestMapping("getCategoryInfoByCategoryId")
	public ResponseVO getCategoryInfoByCategoryId(String categoryId) {
		return getSuccessResponseVO(this.categoryInfoService.getCategoryInfoByCategoryId(categoryId));
	}

	/**
	 * @description 根据 CategoryId更新
	 */
	@RequestMapping("updateCategoryInfoByCategoryId")
	public ResponseVO updateCategoryInfoByCategoryId(CategoryInfo bean, String categoryId) {
		this.categoryInfoService.updateCategoryInfoByCategoryId(bean, categoryId);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 根据 CategoryId删除
	 */
	@RequestMapping("deleteCategoryInfoByCategoryId")
	public ResponseVO deleteCategoryInfoByCategoryId(String categoryId) {
		this.categoryInfoService.deleteCategoryInfoByCategoryId(categoryId);
		return getSuccessResponseVO(null);
	}


	/**
	 * @description 根据 CategoryCode查询
	 */
	@RequestMapping("getCategoryInfoByCategoryCode")
	public ResponseVO getCategoryInfoByCategoryCode(String categoryCode) {
		return getSuccessResponseVO(this.categoryInfoService.getCategoryInfoByCategoryCode(categoryCode));
	}

	/**
	 * @description 根据 CategoryCode更新
	 */
	@RequestMapping("updateCategoryInfoByCategoryCode")
	public ResponseVO updateCategoryInfoByCategoryCode(CategoryInfo bean, String categoryCode) {
		this.categoryInfoService.updateCategoryInfoByCategoryCode(bean, categoryCode);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 根据 CategoryCode删除
	 */
	@RequestMapping("deleteCategoryInfoByCategoryCode")
	public ResponseVO deleteCategoryInfoByCategoryCode(String categoryCode) {
		this.categoryInfoService.deleteCategoryInfoByCategoryCode(categoryCode);
		return getSuccessResponseVO(null);
	}

}