package com.blog.mapper;

import com.blog.entity.Category;

/**
 * @author: zhang
 * @time: 2024-09-14 11:19
 */
public interface CategoryMapper {
    /**
     * 根据主键删除
     * @param categoryId 分类id
     * @return int
     */
    int deleteByPrimaryKey(Integer categoryId);

    /**
     * 插入分类信息
     * @param record 分类信息
     * @return int
     */
    int insert(Category record);

    /**
     * 根据输入选择性地插入分类信息
     * @param record 分类信息
     * @return int
     */
    int insertSelective(Category record);

    /**
     * 根据主键查询分类信息
     * @param categoryId 分类id
     * @return Category
     */
    Category selectByPrimaryKey(Integer categoryId);

    /**
     * 根据主键选择性地更新分类信息
     * @param record 分类信息
     * @return int
     */
    int increatCategoryRank(Category record);

    /**
     * 根据主键选择性地更新分类信息
     * @param record 分类信息
     * @return int
     */
    int updateByPrimaryKey(Category record);
}