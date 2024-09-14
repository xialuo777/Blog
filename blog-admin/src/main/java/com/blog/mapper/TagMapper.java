package com.blog.mapper;

import com.blog.entity.Tag;

import java.util.List;

public interface TagMapper {
    int deleteByPrimaryKey(Long tagId);

    int insert(Tag record);

    int insertSelective(Tag record);

    Tag selectByPrimaryKey(Long tagId);

    int updateByPrimaryKeySelective(Tag record);

    int updateByPrimaryKey(Tag record);

    /**
     * 根据标签名查询标签信息
     *
     * @param tagName 标签名
     * @return Tag
     */
    Tag selectByTagName(String tagName);

    /**
     * 批量插入标签信息
     *
     * @param tagListForInsert 标签信息集合
     */
    void insertList(List<Tag> tagListForInsert);

    /**
     * 根据标签名批量查询标签信息
     *
     * @param tagNames 标签名集合
     * @return List<Tag>
     */
    List<Tag> selectListByTagNames(List<String> tagNames);
}