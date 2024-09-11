package com.blog.controller;

import cn.hutool.core.bean.BeanUtil;
import com.blog.authentication.CurrentUserHolder;
import com.blog.dto.PageResult;
import com.blog.entity.Blog;
import com.blog.enums.ErrorCode;
import com.blog.exception.ResponseResult;
import com.blog.service.BlogService;
import com.blog.vo.blog.BlogDesc;
import com.blog.vo.blog.BlogUpdateVo;
import com.blog.vo.blog.BlogVo;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/blogs")
@RequiredArgsConstructor
public class BlogController {
    private final CurrentUserHolder currentUserHolder;
    private final BlogService blogService;

    @PostMapping("/save")
    public ResponseResult<String> saveBlog(@RequestBody BlogVo blogVo) {
        Blog blog = new Blog();
        BeanUtil.copyProperties(blogVo, blog);
        blogService.saveBlog(blog);
        return ResponseResult.success("文章保存成功");
    }

    @PutMapping("/update")
    public ResponseResult<String> updateBlog(@RequestBody BlogUpdateVo blogUpdateVo) {
        if (blogUpdateVo.getBlogId()==null){
            return ResponseResult.fail(ErrorCode.PARAMS_ERROR.getCode(), "修改文章id不能为空");
        }
        Blog blog = new Blog();
        BeanUtil.copyProperties(blogUpdateVo, blog);
        blogService.updateBlog(blog);
        return ResponseResult.success("文章更新成功");
    }

    @DeleteMapping("/delete/{blogId}")
    public ResponseResult<String> deleteBlog(@PathVariable Long blogId) {
        if (blogService.getBlogById(blogId) == null) {
            return ResponseResult.fail("文章不存在");
        }
        blogService.deleteBlog(blogId);
        return ResponseResult.success("删除成功");
    }

    @GetMapping("/list")
    public ResponseResult<PageResult<BlogDesc>> getCurrentUserBlogList(@RequestParam int pageNo, @RequestParam int pageSize) {
        Long userId = currentUserHolder.getUserId();
        List<Blog> blogList = blogService.getBlogList(userId, pageNo, pageSize);
        if (CollectionUtils.isEmpty(blogList)) {
            return ResponseResult.fail("当前用户博客列表为空");
        }
        List<BlogDesc> blogDescList = blogList.stream()
                .map(blog ->BeanUtil.copyProperties(blog, BlogDesc.class))
                .collect(Collectors.toList());
        int totalCount = blogList.size();
        PageResult<BlogDesc> pageResult = new PageResult<>(blogDescList, totalCount);
        return ResponseResult.success(pageResult);
    }

    @GetMapping("/{blogId}")
    public ResponseResult<BlogVo> getBlog(@PathVariable Long blogId) {
        Blog blog = blogService.getBlogById(blogId);
        if (blog == null){
            return ResponseResult.fail("文章不存在");
        }
        BlogVo blogVo = new BlogVo();
        BeanUtil.copyProperties(blog, blogVo);
        return ResponseResult.success(blogVo);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseResult<List<BlogVo>> getBlogListByCategoryId(@PathVariable Long categoryId, @RequestParam int pageNo, @RequestParam int pageSize) {
        List<Blog> blogList = blogService.getBlogListByCategoryId(categoryId, pageNo, pageSize);
        if (CollectionUtils.isEmpty(blogList)) {
            return ResponseResult.fail("该分类下暂无文章");
        }
        List<BlogVo> blogVoList = blogList.stream()
                .map(blog -> BeanUtil.copyProperties(blog, BlogVo.class))
                .collect(Collectors.toList());
        return ResponseResult.success(blogVoList);
    }
}
