package com.blog.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.blog.dto.PageRequest;
import com.blog.dto.PageResult;
import com.blog.entity.User;
import com.blog.exception.ResponseResult;
import com.blog.service.UserService;
import com.blog.vo.user.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class BaseController {
    @Autowired
    private UserService userService;

    /**
     * 根据昵称查询用户
     * @param nickName
     * @param params
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
     * @param params
     * @return ResponseResult
     * @author zhang
     */
    @GetMapping("/getUsers")
    public ResponseResult<Object> getUsers(@RequestParam Map<String, Object> params) {
        if (CollectionUtil.isEmpty(params)){
            return ResponseResult.fail("分页参数为空");
        }
        PageRequest pageRequest = new PageRequest(params);
        List<User> users = userService.getUsers(pageRequest.getPageNo(), pageRequest.getPageSize());
        List<UserVo> result = users.stream()
                .map(user -> BeanUtil.copyProperties(user, UserVo.class))
                .collect(Collectors.toList());
        int totalCount = userService.getTotalCount();
        PageResult<UserVo> pageResult = new PageResult<>(result, totalCount);
        return ResponseResult.success(pageResult);
    }



}
