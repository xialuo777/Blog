package com.blog.mapper;

import com.blog.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagMapper {
    int insert(Tag record);

    int insertSelective(Tag record);
}