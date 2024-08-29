package com.blog.service;

import com.blog.entity.Blog;
import com.blog.mapper.BlogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BlogService {
    @Autowired
    private BlogMapper blogMapper;

    public void addBlog(Blog blog) {

    }
}
