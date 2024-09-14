package com.blog.controller;


import com.blog.authentication.CurrentUserHolder;
import com.blog.constant.Constant;
import com.blog.entity.Blog;

import com.blog.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author: zhang
 * @time: 2024-09-14 10:30
 */
@RestController
public class BaseController {
    @Autowired
    private CurrentUserHolder currentUserHolder;
    protected boolean validPageParams(Map<String, Object> params) {
        if (ObjectUtils.isEmpty(params.get(Constant.PAGE_NO)) || ObjectUtils.isEmpty(params.get(Constant.PAGE_SIZE))) {
            return false;
        }
        return true;
    }
    protected boolean validUserStatus(User user) {
        return user.getStatus().equals(Constant.USER_STATUS_NORMAL);
    }
    protected boolean validBlogEnableComment(Blog blog) {
        return blog.getEnableComment().equals(Constant.BLOG_ENABLE_COMMENT_YES);
    }
    protected boolean isValidUser(Long userId){
        return currentUserHolder.getUserId().equals(userId);
    }

}
