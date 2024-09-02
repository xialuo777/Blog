package com.blog.mapper;

import com.blog.entity.Blog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BlogMapper {
    int insert(Blog record);

    int insertSelective(Blog record);
}