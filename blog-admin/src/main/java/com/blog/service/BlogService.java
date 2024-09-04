package com.blog.service;


import com.blog.authentication.CurrentUserHolder;
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
import com.blog.util.SnowFlakeUtil;
import com.blog.vo.BlogCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlogService {
    private final CategoryMapper categoryMapper;
    private final BlogMapper blogMapper;
    private final TagMapper tagMapper;
    private final BlogTagMapper blogTagMapper;

    private final CurrentUserHolder currentUserHolder;
    private final int MAX_TAG_COUNT = 6;

    /**
     * 新增博客，同时对博客的标签以及分类等关系进行处理
     *
     * @param blog
     */
    @Transactional
    public void saveBlog(Blog blog) {
        Integer categoryId = blog.getCategoryId();
        Category categoryExist = categoryMapper.selectByPrimaryKey(categoryId);
        Long userId = currentUserHolder.getUserId();
        Long blogId = SnowFlakeUtil.nextId();
        String baseUrl = "https://www.blog.com/blogs/";
        String baseHomePageUrl = baseUrl + userId + "/" + blogId;
        blog.setSubUrl(baseHomePageUrl);
        blog.setUserId(userId);
        blog.setBlogId(blogId);
        if (blogMapper.insertSelective(blog) < 0){
            log.error("文章发布提交至数据库失败");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章发布提交至数据库失败");
        }
        //处理博客分类信息
        handleCategoryAndBlog(blog, categoryExist);
        //处理博客标签信息
        handleTags(blog);
        log.info("文章提交成功");
    }

    @Transactional
    public void updateBlog(Blog blog) {
        Blog blogForUpdate = blogMapper.selectByPrimaryKey(blog.getBlogId());
        blogForUpdate.setBlogTitle(blog.getBlogTitle());
        blogForUpdate.setBlogContent(blog.getBlogContent());
        blogForUpdate.setThumbnail(blog.getThumbnail());
        blogForUpdate.setBlogStatus(blog.getBlogStatus());
        blogForUpdate.setEnableComment(blog.getEnableComment());
        blogForUpdate.setBlogDesc(blog.getBlogDesc());
        blogForUpdate.setIsTop(blog.getIsTop());
        blogForUpdate.setBlogTags(blog.getBlogTags());
        blogForUpdate.setCategoryId(blog.getCategoryId());
        blogForUpdate.setCategoryName(blog.getCategoryName());
        Category categoryExist = categoryMapper.selectByPrimaryKey(blog.getCategoryId());
        if (blogMapper.insertSelective(blogForUpdate)<0) {
            log.error("文章更新提交至数据库失败");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章更新提交至数据库失败");
        }
        //处理博客分类关系
        handleCategoryAndBlog(blogForUpdate, categoryExist);
        //处理博客分类信息
        handleTags(blogForUpdate);
        log.info("文章更新成功");

    }

    /**
     * 处理博客分类信息
     *
     * @param blog
     * @param categoryExist
     * @return Blog
     */
    private void handleCategoryAndBlog(Blog blog, Category categoryExist) {
        /*如果分类是新增分类，需要输入categoryId和categoryName信息
         * 如果分类信息在分类表中存在，则对分类表rank进行更新*/
        if (categoryExist == null) {
            String categoryName = Optional.ofNullable(blog.getCategoryName())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "请输入新增分类名"));
            Category category = new Category(blog.getCategoryId(), categoryName);
            categoryMapper.insertSelective(category);
        } else {
            blog.setCategoryName(categoryExist.getCategoryName());
            categoryExist.setCategoryRank(categoryExist.getCategoryRank() + 1);
            categoryMapper.updateByPrimaryKeySelective(categoryExist);
        }
    }

    /**
     * 处理博客标签信息
     *
     * @param blog
     */
    private void handleTags(Blog blog) {
        if (blog.getBlogTags() != null) {
            String[] tags = blog.getBlogTags().split(",");
            if (tags.length > MAX_TAG_COUNT) {
                log.error("输入标签数量限制为6，请重新输入");
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入标签数量限制为6，请重新输入");
            }
            List<Tag> tagListForInsert = new ArrayList<>();
            List<Tag> allTagsList = new ArrayList<>();
            for (String tagName : tags) {
                Tag tag = tagMapper.selectByTagName(tagName);
                if (tag == null) {
                    tag = new Tag(tagName);
                    tagListForInsert.add(tag);
                } else {
                    allTagsList.add(tag);
                }
            }
            allTagsList.addAll(tagListForInsert);
            if (!CollectionUtils.isEmpty(tagListForInsert)) {
                tagMapper.insertList(tagListForInsert);
            }
            List<BlogTag> blogTags = createBlogTags(blog, allTagsList);
            blogTagMapper.insertList(blogTags);
        }
    }

    /**
     * 处理博客标签关系，即博客与标签的关联表
     *
     * @param blog
     * @param tags
     * @return List<BlogTag>
     */
    private List<BlogTag> createBlogTags(Blog blog, List<Tag> tags) {
        List<BlogTag> blogTags = tags.stream()
                .map(tag -> new BlogTag(blog.getBlogId(), tag.getTagId()))
                .collect(Collectors.toList());
        return blogTags;
    }

}
