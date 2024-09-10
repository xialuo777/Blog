package com.blog.controller;

import cn.hutool.core.bean.BeanUtil;
import com.blog.authentication.CurrentUserHolder;
import com.blog.entity.Blog;
import com.blog.exception.ResponseResult;
import com.blog.service.BlogService;
import com.blog.vo.blog.BlogDesc;
import com.blog.vo.blog.BlogUpdateVo;
import com.blog.vo.blog.BlogVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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
    public ResponseResult<List<BlogDesc>> getBlogList(@RequestParam int pageNo, @RequestParam int pageSize) {
        Long userId = currentUserHolder.getUserId();
        List<Blog> blogList = blogService.getBlogList(userId, pageNo, pageSize);
        if (blogList == null || blogList.isEmpty()) {
            return ResponseResult.fail("当前用户暂未发表文章");
        }
        List<BlogDesc> blogDescList = new ArrayList<>(blogList.size());
        for (Blog blog : blogList) {
            BlogDesc blogDesc = new BlogDesc();
            BeanUtil.copyProperties(blog, blogDesc);
            blogDescList.add(blogDesc);
        }
        return ResponseResult.success(blogDescList);
    }

    @GetMapping("/{blogId}")
    public ResponseResult<BlogVo> getBlog(@PathVariable Long blogId) {
        Blog blog = blogService.getBlogById(blogId);
        BlogVo blogVo = new BlogVo();
        BeanUtil.copyProperties(blog, blogVo);
        return ResponseResult.success(blogVo);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseResult<List<BlogVo>> getBlogListByCategoryId(@PathVariable Long categoryId, @RequestParam int pageNo, @RequestParam int pageSize) {
        List<Blog> blogList = blogService.getBlogListByCategoryId(categoryId, pageNo, pageSize);
        if (blogList == null || blogList.isEmpty()) {
            return ResponseResult.fail("该分类下暂无文章");
        }
        List<BlogVo> blogVoList = new ArrayList<>(blogList.size());
        for (Blog blog : blogList) {
            BlogVo blogVo = new BlogVo();
            BeanUtil.copyProperties(blog, blogVo);
        }
        return ResponseResult.success(blogVoList);
    }
}
