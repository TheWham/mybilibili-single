package com.easylive.service.impl;

import com.easylive.entity.po.CategoryInfo;
import com.easylive.entity.query.CategoryInfoQuery;
import com.easylive.entity.query.SimplePage;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.enums.PageSize;
import com.easylive.mappers.CategoryInfoMapper;
import com.easylive.service.CategoryInfoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author amani
 * @date 2026/01/19
 * @description 分类信息
Service
 */

@Service("CategoryInfoService")
public class CategoryInfoServiceImpl implements CategoryInfoService {
	@Resource
	private CategoryInfoMapper<CategoryInfo, CategoryInfoQuery> categoryInfoMapper;

	/**
	 * @description 根据条件查询
	 */
	@Override
	public List<CategoryInfo> findListByParam(CategoryInfoQuery param) {
		return this.categoryInfoMapper.selectList(param);
	}

	/**
	 * @description 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(CategoryInfoQuery param) {
		return this.categoryInfoMapper.selectCount(param);
	}

	/**
	 * @description 分页查询
	 */
	@Override
	public PaginationResultVO<CategoryInfo> findListByPage(CategoryInfoQuery param) {
		Integer count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<CategoryInfo> list = this.findListByParam(param);
		PaginationResultVO<CategoryInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * @description 新增
	 */
	@Override
	public Integer add(CategoryInfo bean) {
		return this.categoryInfoMapper.insert(bean);
	}

	/**
	 * @description 批量新增
	 */
	@Override
	public Integer addBatch(List<CategoryInfo>  listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.categoryInfoMapper.insertBatch(listBean);
	}

	/**
	 * @description 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<CategoryInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.categoryInfoMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * @description 根据 CategoryId查询
	 */
	@Override
	public CategoryInfo getCategoryInfoByCategoryId(String categoryId) {
		return this.categoryInfoMapper.selectByCategoryId(categoryId);
	}

	/**
	 * @description 根据 CategoryId更新
	 */
	@Override
	public Integer updateCategoryInfoByCategoryId(CategoryInfo bean, String categoryId) {
		return this.categoryInfoMapper.updateByCategoryId(bean, categoryId);
	}

	/**
	 * @description 根据 CategoryId删除
	 */
	@Override
	public Integer deleteCategoryInfoByCategoryId(String categoryId) {
		return this.categoryInfoMapper.deleteByCategoryId(categoryId);
	}


	/**
	 * @description 根据 CategoryCode查询
	 */
	@Override
	public CategoryInfo getCategoryInfoByCategoryCode(String categoryCode) {
		return this.categoryInfoMapper.selectByCategoryCode(categoryCode);
	}

	/**
	 * @description 根据 CategoryCode更新
	 */
	@Override
	public Integer updateCategoryInfoByCategoryCode(CategoryInfo bean, String categoryCode) {
		return this.categoryInfoMapper.updateByCategoryCode(bean, categoryCode);
	}

	/**
	 * @description 根据 CategoryCode删除
	 */
	@Override
	public Integer deleteCategoryInfoByCategoryCode(String categoryCode) {
		return this.categoryInfoMapper.deleteByCategoryCode(categoryCode);
	}

}
