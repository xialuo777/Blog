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
import com.blog.util.redis.RedisProcessor;
import com.blog.util.redis.RedisTransKey;
import com.blog.vo.admin.AdminVoIn;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;



@RequestMapping("/admin")
@RestController
@RequiredArgsConstructor
public class AdminController extends BaseController{
    private final UserService userService;
    private final AdminService adminService;
    private final BlogService blogService;
    private final CurrentUserHolder currentUserHolder;
    private final JwtProcessor jwtProcessor;
    private final RedisProcessor redisProcessor;


    /**
     * 管理员账号登录
     * @param adminVoIn
     * @return ResponseResult
     * @author zhang
     */
    @PostMapping("/login")
    public ResponseResult<LoginResponse> login(@RequestBody AdminVoIn adminVoIn) {
        LoginResponse loginResponse = adminService.adminLogin(adminVoIn);
        return ResponseResult.success(loginResponse);
    }

    /**
     * 更新管理员账号信息
     * @param adminVoIn
     * @return ResponseResult
     * @author zhang
     */
    @PutMapping("/update")
    public ResponseResult<String> updateAdmin(@RequestBody AdminVoIn adminVoIn) {
        String accessToken = (String) redisProcessor.get(RedisTransKey.getTokenKey(adminVoIn.getAccount()));
        Long adminId = currentUserHolder.getUserId();
        if (!jwtProcessor.validateToken(accessToken, adminId)) {
            return ResponseResult.fail(ErrorCode.TOKEN_ERROR.getCode(), "token验证失败");
        }
        Admin admin = adminService.getAdminById(adminId).orElseThrow(() -> new BusinessException("该管理员账户不存在"));

        BeanUtil.copyProperties(adminVoIn, admin, CopyOptions.create().setIgnoreNullValue(true).setIgnoreCase(true));
        adminService.updateAdmin(admin);
        return ResponseResult.success("管理员信息更新成功");
    }


    /**
     * 根据用户id查找用户
     * @param userId
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
     * @param userId
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
     * @param userId
     * @param status
     * @return
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
     * 根据博客id删除博客
     * @param blogId
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


}
