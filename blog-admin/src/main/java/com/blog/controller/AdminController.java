package com.blog.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.blog.authentication.CurrentUserHolder;
import com.blog.entity.Admin;
import com.blog.entity.User;
import com.blog.enums.ErrorCode;
import com.blog.exception.BusinessException;
import com.blog.exception.ResponseResult;
import com.blog.service.AdminService;
import com.blog.service.BlogService;
import com.blog.service.UserService;
import com.blog.util.JwtProcessor;
import com.blog.util.bo.LoginResponse;
import com.blog.util.dto.PageRequest;
import com.blog.util.dto.PageResult;
import com.blog.util.redis.RedisProcessor;
import com.blog.util.redis.RedisTransKey;
import com.blog.vo.admin.AdminInVo;
import com.blog.vo.blog.BlogDesc;
import com.blog.vo.user.UserVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author: zhang
 * @time: 2024-09-14 10:26
 */
@RequestMapping("/admin")
@RestController
@RequiredArgsConstructor
public class AdminController extends BaseController{
    private final UserService userService;
    private final AdminService adminService;
    private final CurrentUserHolder currentUserHolder;
    private final JwtProcessor jwtProcessor;
    private final RedisProcessor redisProcessor;

    private final BlogService blogService;


    /**
     * 管理员账号登录
     * @param adminInVo  用于接收管理员输入的信息对象，包含管理员操作所需的各种数据
     * @return ResponseResult
     * @author zhang
     */
    @PostMapping("/login")
    public ResponseResult<LoginResponse> login(@RequestBody AdminInVo adminInVo) {
        LoginResponse loginResponse = adminService.adminLogin(adminInVo);
        return ResponseResult.success(loginResponse);
    }

    /**
     * 更新管理员账号信息
     * @param adminInVo  用于接收管理员输入的信息对象，包含管理员操作所需的各种数据
     * @return ResponseResult
     * @author zhang
     */
    @PutMapping("/update")
    public ResponseResult<String> updateAdmin(@RequestBody AdminInVo adminInVo) {
        String accessToken = (String) redisProcessor.get(RedisTransKey.getTokenKey(adminInVo.getAccount()));
        Long adminId = currentUserHolder.getUserId();
        if (!jwtProcessor.validateToken(accessToken, adminId)) {
            return ResponseResult.fail(ErrorCode.TOKEN_ERROR.getCode(), "token验证失败");
        }
        Admin admin = adminService.getAdminById(adminId).orElseThrow(() -> new BusinessException("该管理员账户不存在"));

        BeanUtil.copyProperties(adminInVo, admin, CopyOptions.create().setIgnoreNullValue(true).setIgnoreCase(true));
        adminService.updateAdmin(admin);
        return ResponseResult.success("管理员信息更新成功");
    }


    /**
     * 根据用户id查找用户
     * @param userId 用于接收查找用户的id信息
     * @return ResponseResult
     * @author zhang
     */
    @GetMapping("/user/{userId}")
    public ResponseResult<User> getUser(@PathVariable Long userId) {
        return ResponseResult.success(userService.selectUserByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在")));
    }

    /**
     * 删除用户信息
     * @param userId  用于接收删除用户的id信息
     * @return ResponseResult
     * @author zhang
     */
    @DeleteMapping("/user/delete/{userId}")
    public ResponseResult<String> delete(@PathVariable Long userId) {
        userService.selectUserByUserId(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        userService.deleteUserById(userId);
        return ResponseResult.success("用户删除成功");
    }

    /**
     * 修改用户状态，0为正常 1为封禁
     * @param userId  用于接收修改用户信息的id信息
     * @param status  用于接收修改用户状态的信息
     * @return ResponseResult
     */
    @PutMapping("/user/status")
    public ResponseResult<String> updateUserStatus(@RequestParam Long userId, @RequestParam Integer status) {
        User user = userService.selectUserByUserId(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setStatus(status);
        userService.updateUser(user);
        return ResponseResult.success("用户状态更新成功");
    }

    /**
     * 根据昵称查询用户
     * @param nickName 接收查询用户的昵称信息
     * @param params 接受列表查询的分页信息
     * @return ResponseResult
     * @author zhang
     */
    @GetMapping("/{nickName}")
    public ResponseResult<PageResult<UserVo>> getUsersByNickName(@PathVariable String nickName, @RequestParam Map<String, Object> params) {
        if (!validPageParams(params)){
            return ResponseResult.fail("分页参数异常");
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
        if (!validPageParams(params)){
            return ResponseResult.fail("分页参数异常");
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
        if (!validPageParams(params)){
            return ResponseResult.fail("分页参数异常");
        }
        PageRequest pageRequest = new PageRequest(params);
        List<BlogDesc> blogDescList = blogService.getBlogList(pageRequest.getPageNo(), pageRequest.getPageSize())
                .orElseThrow(() -> new BusinessException("博客列表为空"));
        return ResponseResult.success(blogDescList);
    }

}
