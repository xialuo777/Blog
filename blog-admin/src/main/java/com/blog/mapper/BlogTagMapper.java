package com.blog.mapper;

import com.blog.entity.BlogTag;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BlogTagMapper {
    int insert(BlogTag record);

    int insertSelective(BlogTag record);
}