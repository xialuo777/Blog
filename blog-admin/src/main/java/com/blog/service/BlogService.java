package com.blog.service;


import com.blog.entity.Blog;
import com.blog.entity.BlogTag;
import com.blog.entity.Category;
import com.blog.entity.Tag;
import com.blog.enums.ErrorCode;
import com.blog.exception.BusinessException;
import com.blog.mapper.BlogMapper;
import com.blog.mapper.BlogTagMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlogService {
    private final CategoryMapper categoryMapper;
    private final BlogMapper blogMapper;
    private final TagMapper tagMapper;
    private final BlogTagMapper blogTagMapper;

    @Transactional
    public void saveBlog(Blog blog) {
        //查找用户博客选择分类
        Category category = categoryMapper.selectByPrimaryKey(blog.getCategoryId());
        //用户选择博客分类在category表中不存在，则默认其选择默认分类
        if (category == null) {
            blog.setCategoryId(0);
            blog.setCategoryName("默认分类");
        }
        //选择了category表中的分类，则将分类名保存到blog对象中
        blog.setCategoryName(category.getCategoryName());
        category.setCategoryRank(category.getCategoryRank() + 1);
        categoryMapper.updateByPrimaryKeySelective(category);
        //处理标签数据，限制添加标签数量为6
        String[] tags = blog.getBlogTags().split(",");
        if (tags.length > 6) {
            log.error("输入标签数量限制为6，请重新输入");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"输入标签数量限制为6，请重新输入");
        }
        //保存文章，同时对新增tags数据进行处理，添加到数据库中
        if (blogMapper.insertSelective(blog) > 0) {
            //新增的tag对象，添加到tag表中
            List<Tag> tagListForInsert = new ArrayList<>();
            //所有的tag对象，用于建立关系数据blogTag
            List<Tag> allTagsList = new ArrayList<>();
            for (int i = 0; i < tags.length; i++) {
                Tag tag = tagMapper.selectByTagName(tags[i]);
                if (tag == null) {
                    //不存在就新增
                    Tag tempTag = new Tag();
                    tempTag.setTagName(tags[i]);
                    tagListForInsert.add(tempTag);
                } else {
                    allTagsList.add(tag);
                }
            }
            if (!CollectionUtils.isEmpty(tagListForInsert)) {
                //新增标签数据
                tagMapper.insertList(tagListForInsert);
            }
            List<BlogTag> blogTags = new ArrayList<>();
            for (Tag tag : allTagsList) {
                BlogTag blogTag = new BlogTag();
                blogTag.setBlogId(blog.getBlogId());
                blogTag.setTagId(tag.getTagId());
                blogTags.add(blogTag);
            }
            if (blogTagMapper.insertList(blogTags) < 0){
                log.error("标签插入失败");
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"标签插入失败");
            }
            log.info("文章提交成功");
        }
        log.error("文章提交失败");

    }

}
