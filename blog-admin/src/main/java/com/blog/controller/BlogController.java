package com.blog.controller;

import com.blog.entity.Blog;
import com.blog.service.BlogService;
import com.blog.util.BeanCopyUtils;
import com.blog.util.SnowFlakeUtil;
import com.blog.vo.BlogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/blog")
@RequiredArgsConstructor
public class BlogController {
    private final BlogService blogService;
    @RequestMapping("/save")
    public ResponseEntity saveBlog(@RequestBody BlogVO blogVO, HttpServletRequest request) {
        Blog blog = BeanCopyUtils.copyBean(blogVO, Blog.class);
        blog.setBlogId(SnowFlakeUtil.getInstance().nextId());
        blog.setUserId((Long) request.getSession().getAttribute("userId"));
        blogService.saveBlog(blog);
        return ResponseEntity.ok("文章保存成功");
    }
}
