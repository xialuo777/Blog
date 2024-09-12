package com.blog.controller;

import cn.hutool.core.bean.BeanUtil;
import com.blog.authentication.CurrentUserHolder;
import com.blog.dto.PageResult;
import com.blog.entity.Blog;
import com.blog.entity.User;

import com.blog.exception.BusinessException;
import com.blog.exception.ResponseResult;
import com.blog.service.BlogService;
import com.blog.service.UserService;
import com.blog.vo.blog.BlogDesc;
import com.blog.vo.blog.BlogDetail;
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

    private final UserService userService;
    private final BlogService blogService;

    @PostMapping("/save")
    public ResponseResult<String> saveBlog(@RequestBody BlogVo blogVo) {
        User user = userService.selectUserByUserId(blogVo.getUserId())
                .orElseThrow(() -> new BusinessException("用户不存在！"));
        if (user.getStatus()==1){
            return ResponseResult.fail("该用户已被封禁，无法发布文章！");
        }
        Blog blog = new Blog();
        BeanUtil.copyProperties(blogVo, blog);
        blogService.saveBlog(blog);
        return ResponseResult.success("文章保存成功");
    }

    @PutMapping("/update/{blogId}")
    public ResponseResult<String> updateBlog(@RequestBody BlogUpdateVo blogUpdateVo, @PathVariable Long blogId) {
        Blog blog = blogService.getBlogById(blogId)
                .orElseThrow(() -> new BusinessException("文章不存在！"));
        BeanUtil.copyProperties(blogUpdateVo, blog);
        blogService.updateBlog(blog);
        return ResponseResult.success("文章更新成功");
    }

    @DeleteMapping("/delete/{blogId}")
    public ResponseResult<String> deleteBlog(@PathVariable Long blogId) {
        blogService.getBlogById(blogId)
                .orElseThrow(() -> new BusinessException("文章不存在！"));
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
                .map(blog -> BeanUtil.copyProperties(blog, BlogDesc.class))
                .collect(Collectors.toList());
        int totalCount = blogList.size();
        PageResult<BlogDesc> pageResult = new PageResult<>(blogDescList, totalCount);
        return ResponseResult.success(pageResult);
    }

    @GetMapping("/{blogId}")
    public ResponseResult<BlogDetail> getBlog(@PathVariable Long blogId) {
        Blog blog = blogService.getBlogById(blogId)
                .orElseThrow(() -> new BusinessException("文章不存在！"));
        BlogDetail blogDetail = new BlogDetail();
        BeanUtil.copyProperties(blog, blogDetail);
        return ResponseResult.success(blogDetail);
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
