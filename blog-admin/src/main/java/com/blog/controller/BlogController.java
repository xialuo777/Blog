package com.blog.controller;

import com.blog.entity.Blog;
import com.blog.service.BlogService;
import com.blog.vo.blog.BlogUpdateVo;
import com.blog.vo.blog.BlogVo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/blogs")
@RequiredArgsConstructor
public class BlogController {
    private final BlogService blogService;
    @PostMapping("/save")
    public ResponseEntity<String> saveBlog(@RequestBody BlogVo blogVo){
        blogService.saveBlog(blogVo);
        return ResponseEntity.ok("文章保存成功");
    }
    @PutMapping("/update")
    public ResponseEntity<String> updateBlog(@RequestBody BlogUpdateVo blogUpdateVo){
        blogService.updateBlog(blogUpdateVo);
        return ResponseEntity.ok("文章更新成功");
    }
}
