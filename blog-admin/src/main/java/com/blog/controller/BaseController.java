package com.blog.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.blog.exception.BusinessException;
import com.blog.service.BlogService;
import com.blog.util.dto.PageRequest;
import com.blog.util.dto.PageResult;
import com.blog.entity.User;
import com.blog.exception.ResponseResult;
import com.blog.service.UserService;
import com.blog.vo.blog.BlogDesc;
import com.blog.vo.user.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: zhang
 * @time: 2024-09-14 10:30
 */
@RestController
public class BaseController {
    @Autowired
    private UserService userService;
    @Autowired
    private BlogService blogService;

    /**
     * 根据昵称查询用户
     * @param nickName 接收查询用户的昵称信息
     * @param params 接受列表查询的分页信息
     * @return ResponseResult
     * @author zhang
     */
    @GetMapping("/{nickName}")
    public ResponseResult<PageResult<UserVo>> getUsersByNickName(@PathVariable String nickName, @RequestParam Map<String, Object> params) {
        if (CollectionUtil.isEmpty(params)){
            return ResponseResult.fail("分页参数为空");
        }
        PageRequest pageRequest = new PageRequest(params);
        List<User> users = userService.selectUsersByNickName(nickName, pageRequest.getPageNo(), pageRequest.getPageSize());
        List<UserVo> result = users.stream().map(user -> BeanUtil.copyProperties(user, UserVo.class))
                .collect(Collectors.toList());
        int totalCount = result.size();
        PageResult<UserVo> pageResult = new PageResult<>(result, totalCount);
        return ResponseResult.success(pageResult);
    }

    /**
     * 获取所有用户列表
     * @param params 接受列表查询的分页信息
     * @return ResponseResult
     * @author zhang
     */
    @GetMapping("/getUsers")
    public ResponseResult<Object> getUsers(@RequestParam Map<String, Object> params) {
        if (CollectionUtil.isEmpty(params)){
            return ResponseResult.fail("分页参数为空");
        }
        PageRequest pageRequest = new PageRequest(params);
        List<User> users = userService.getUsers(pageRequest.getPageNo(), pageRequest.getPageSize())
                .orElseThrow(() -> new BusinessException("用户列表为空"));
        List<UserVo> result = users.stream()
                .map(user -> BeanUtil.copyProperties(user, UserVo.class))
                .collect(Collectors.toList());
        int totalCount = userService.getTotalCount();
        PageResult<UserVo> pageResult = new PageResult<>(result, totalCount);
        return ResponseResult.success(pageResult);
    }


    /**
     * 根据博客id删除博客
     * @param blogId 接收删除博客的id信息
     * @return ResponseResult
     * @author zhang
     */
    @DeleteMapping("/delete/{blogId}")
    public ResponseResult<String> deleteBlog(@PathVariable Long blogId) {
        blogService.getBlogById(blogId)
                .orElseThrow(() -> new BusinessException("文章不存在！"));
        blogService.deleteBlog(blogId);
        return ResponseResult.success("删除成功");
    }

    /**
     * 获取所有博客列表
     * @param params 接受列表查询的分页信息
     * @return ResponseResult
     */
    @GetMapping("/blog/list")
    public ResponseResult<List<BlogDesc>> getBlogList(@RequestParam Map<String, Object> params) {
        if(CollectionUtil.isEmpty(params)){
            return ResponseResult.fail("分页参数为空");
        }
        PageRequest pageRequest = new PageRequest(params);
        List<BlogDesc> blogDescList = blogService.getBlogList(pageRequest.getPageNo(), pageRequest.getPageSize())
                .orElseThrow(() -> new BusinessException("博客列表为空"));
        return ResponseResult.success(blogDescList);
    }

}
