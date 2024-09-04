package com.blog.controller;

import com.blog.authentication.CurrentUserHolder;
import com.blog.entity.Blog;
import com.blog.service.BlogService;
import com.blog.util.SnowFlakeUtil;
import com.blog.vo.BlogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/blogs")
@RequiredArgsConstructor
public class BlogController {
    private final BlogService blogService;
    @PostMapping("/save")
    public ResponseEntity<String> saveBlog(@RequestBody Blog blog){
        blogService.saveBlog(blog);
        return ResponseEntity.ok("文章保存成功");
    }
    @PutMapping("/update")
    public ResponseEntity<String> updateBlog(@RequestBody Blog blog){
        blogService.updateBlog(blog);
        return ResponseEntity.ok("文章更新成功");
    }
}
