package com.blog.mapper;

import com.blog.entity.Category;

public interface CategoryMapper {
    int deleteByPrimaryKey(Integer categoryId);

    int insert(Category record);

    int insertSelective(Category record);

    Category selectByPrimaryKey(Integer categoryId);

    int increatCategoryRank(Category record);

    int updateByPrimaryKey(Category record);
}