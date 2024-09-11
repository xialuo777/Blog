package com.blog.service;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.blog.authentication.CurrentUserHolder;
import com.blog.constant.Constant;
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
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
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
        Optional<Category> optionalCategory = Optional.ofNullable(categoryExist);
        Long userId = currentUserHolder.getUserId();
        Long blogId = SnowFlakeUtil.nextId();
        String baseHomePageUrl = String.format(Constant.BLOG_BASE_PATH + "%s/%s", userId, blogId);
        blog.setSubUrl(baseHomePageUrl);
        blog.setUserId(userId);
        blog.setBlogId(blogId);
        blogMapper.insertSelective(blog);
        handleCategoryAndBlog(blog, optionalCategory);
        handleTags(blog);
    }

    /**
     * 更新博客，同时对博客的标签以及分类等关系进行处理
     *
     * @param blog
     */
    @Transactional
    public void updateBlog(Blog blog) {
        Blog blogForUpdate = blogMapper.selectByPrimaryKey(blog.getBlogId());
        BeanUtil.copyProperties(blog, blogForUpdate, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
        Category categoryExist = categoryMapper.selectByPrimaryKey(blogForUpdate.getCategoryId());
        Optional<Category> optionalCategory = Optional.ofNullable(categoryExist);
        blogMapper.updateByPrimaryKeySelective(blogForUpdate);
        handleCategoryAndBlog(blogForUpdate, optionalCategory);
        handleTags(blogForUpdate);
    }

    /**
     * 处理博客分类信息
     *
     * @param blog
     * @param categoryExist
     * @return Blog
     */
    private void handleCategoryAndBlog(Blog blog, Optional<Category> categoryExist) {
        /*如果分类是新增分类，需要输入categoryId和categoryName信息
         * 如果分类信息在分类表中存在，则对分类表rank进行更新*/
        if (!categoryExist.isPresent()) {
            Category category = new Category(blog.getCategoryId(), blog.getCategoryName());
            categoryMapper.insertSelective(category);
        } else {
            categoryMapper.increatCategoryRank(categoryExist.get());
        }
    }

    /**
     * 处理博客标签信息
     *
     * @param blog
     */

    private void handleTags(Blog blog) {
        if (StringUtil.isNotEmpty(blog.getBlogTags())) {
            String[] tags = blog.getBlogTags().split(",");
            if (tags.length > MAX_TAG_COUNT) {
                log.error("输入标签数量限制为{}，请重新输入", MAX_TAG_COUNT);
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入标签数量限制为{}，请重新输入", MAX_TAG_COUNT);
            }
            List<String> distinctTagNames = Arrays.stream(tags).distinct().collect(Collectors.toList());


            List<Tag> tagsFromDb = tagMapper.selectListByTagNames(distinctTagNames);
            List<Tag> mutableTagsFromDb = new ArrayList<>(tagsFromDb);
            //处理数据库中并不存在的标签，即需要新插入的标签
            List<Tag> tagListForInsert = distinctTagNames.stream()
                    .filter(tagName -> !mutableTagsFromDb.stream().map(Tag::getTagName).collect(Collectors.toSet()).contains(tagName))
                    .map(tagName -> new Tag(tagName))
                    .collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(tagListForInsert)) {
                tagMapper.insertList(tagListForInsert);
                mutableTagsFromDb.addAll(tagListForInsert); // 将新插入的标签添加到数据库已有的标签列表中
            }

            /* 处理blog - tag 映射关系 */
            List<Tag> allTagsList = mutableTagsFromDb.stream()
                    .collect(Collectors.toList());

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

    public List<Blog> getBlogList(Long userId, int pageNo, int pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<Blog> blogs = blogMapper.selectListByUserId(userId);
        return blogs;
    }

    public Blog getBlogById(Long blogId) {
        Blog blog = blogMapper.selectByPrimaryKey(blogId);
        return blog;
    }

    public List<Blog> getBlogListByCategoryId(Long categoryId, int pageNo, int pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<Blog> blogs = blogMapper.selectListByCategoryId(categoryId);
        return blogs;
    }

    public void deleteBlog(Long blogId) {
        blogMapper.deleteByPrimaryKey(blogId);
    }
}
