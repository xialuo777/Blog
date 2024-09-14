package com.blog.mapper;

import com.blog.entity.Tag;

import java.util.List;

/**
 * @author: zhang
 * @time: 2024-09-14 11:21
 */
public interface TagMapper {
    /**
     * 根据主键删除
     * @param tagId 标签id
     * @return int
     */
    int deleteByPrimaryKey(Integer tagId);

    /**
     * 插入标签信息
     * @param record 标签信息
     * @return int
     */
    int insert(Tag record);

    /**
     * 选择性插入标签信息
     * @param record 标签信息
     * @return int
     */
    int insertSelective(Tag record);

    /**
     * 根据主键查询标签信息
     * @param tagId 标签id
     * @return Tag
     */
    Tag selectByPrimaryKey(Integer tagId);

    /**
     * 根据主键选择性地更新标签信息
     * @param record 标签信息
     * @return int
     */
    int updateByPrimaryKeySelective(Tag record);

    /**
     * 根据主键更新标签信息
     * @param record 标签信息
     * @return int
     */
    int updateByPrimaryKey(Tag record);

    /**
     * 根据标签名查询标签信息
     * @param tagName 标签名
     * @return Tag
     */
    Tag selectByTagName(String tagName);

    /**
     * 批量插入标签信息
     * @param tagListForInsert 标签信息集合
     */
    void insertList(List<Tag> tagListForInsert);

    /**
     * 根据标签名批量查询标签信息
     * @param tagNames 标签名集合
     * @return List<Tag>
     */
    List<Tag> selectListByTagNames(List<String> tagNames);
}