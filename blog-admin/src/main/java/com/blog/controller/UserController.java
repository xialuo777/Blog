package com.blog.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import com.blog.authentication.CurrentUserHolder;
import com.blog.constant.Constant;
import com.blog.entity.User;

import com.blog.enums.ErrorCode;
import com.blog.exception.BusinessException;
import com.blog.exception.ResponseResult;
import com.blog.mapper.UserMapper;
import com.blog.service.MailService;
import com.blog.service.UserService;
import com.blog.util.JwtProcessor;
import com.blog.util.SecurityUtils;
import com.blog.util.UserTransUtils;
import com.blog.util.bo.LoginResponse;
import com.blog.util.dto.PageRequest;
import com.blog.util.dto.PageResult;
import com.blog.util.redis.RedisProcessor;
import com.blog.util.redis.RedisTransKey;
import com.blog.vo.user.UserInfoVo;
import com.blog.vo.user.Loginer;
import com.blog.vo.user.Register;
import com.blog.vo.user.UserVo;
import com.github.pagehelper.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.constraints.Email;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author: zhang
 * @time: 2024-09-14 10:38
 */
@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final UserService userService;
    private final MailService mailService;
    private final RedisProcessor redisProcessor;
    private final UserMapper userMapper;
    private final JwtProcessor jwtProcessor;
    private final CurrentUserHolder currentUserHolder;

    /**
     * 用户注册
     * @param register 用户注册信息
     * @return ResponseResult
     * @author zhang
     */
    @PostMapping("/register")
    public ResponseResult<String> register(@RequestBody @Validated Register register) {
        userService.userRegister(register);
        return ResponseResult.success("用户注册成功");
    }

    /**
     * 用户获取邮箱验证码
     *
     * @param email 用户邮箱
     * @return ResponseResult
     * @author zhang
     */
    @GetMapping("/getCode")
    @ResponseBody
    public ResponseResult<String> getCode(@RequestParam @Email String email) {
        mailService.getEmailCode(email);
        return ResponseResult.success("验证码发送成功");
    }

    /**
     * @param loginer 登录用户信息
     * @return ResponseResult
     * @author zhang
     */
    @PostMapping("/login")
    @ResponseBody
    public ResponseResult<LoginResponse> login(@Validated @RequestBody Loginer loginer) {
        //用户登陆后返回给前端accessToken和refreshToken
        LoginResponse loginResponse = userService.userLogin(loginer);
        return ResponseResult.success(loginResponse);
    }

    /**
     * 刷新token
     *
     * @param refreshToken 刷新令牌
     * @return ResponseResult
     * @author zhang
     */
    @PostMapping("/refresh")
    public ResponseResult<String> refreshToken(String refreshToken) {
        Map<String, Object> userMap = jwtProcessor.extractUserMap(refreshToken);
        Long userId = (Long) userMap.get(Constant.ID);
        User user = userMapper.selectByPrimaryKey(userId);
        if (!validUserStatus(user)) {
            log.error("该用户处于异常状态，无法执行下一步操作");
            throw new BusinessException("该用户处于异常状态，无法执行下一步操作");
        }
        //返回给前端刷新后的accessToken,同时也会产生新的refreshToken,以防refreshToken过期
        String accessToken = userService.refreshAccessToken(refreshToken, userId);
        return ResponseResult.success(accessToken);
    }

    /***
     * 用户退出登陆时，需要删除token信息
     * @return ResponseEntity
     * @author zhang
     */
    @GetMapping("/logout")
    public ResponseResult<String> logout() {
        Long userId = currentUserHolder.getUserId();
        User user = userService.selectUserByUserId(userId)
                .orElseThrow(()->new BusinessException("用户不存在"));
        String accessToken = (String) redisProcessor.get(RedisTransKey.getTokenKey(user.getEmail()));
        if (!jwtProcessor.validateToken(accessToken, userId)) {
            return ResponseResult.fail(ErrorCode.TOKEN_ERROR.getCode(), "token验证失败");
        }

        redisProcessor.del(RedisTransKey.getRefreshTokenKey(user.getEmail()));
        redisProcessor.del(RedisTransKey.getLoginKey(user.getEmail()));
        redisProcessor.del(RedisTransKey.getTokenKey(user.getEmail()));
        log.info("用户退出登陆成功");
        return ResponseResult.success("用户退出登陆成功");
    }

    /**
     * 用户删除登录会先对当前请求中的token进行验证
     * @return ResponseResult
     * @author zhang
     */
    @DeleteMapping("/delete")
    public ResponseResult<String> delete() {
        Long userId = currentUserHolder.getUserId();
        User user = userService.selectUserByUserId(userId)
                .orElseThrow(() -> new BusinessException("用户不存在！"));
        String accessToken = (String) redisProcessor.get(RedisTransKey.getTokenKey(user.getEmail()));
        if (!jwtProcessor.validateToken(accessToken, userId)) {
            return ResponseResult.fail(ErrorCode.TOKEN_ERROR.getCode(), "token验证失败");
        }
        userService.deleteUserById(userId);
        return ResponseResult.success("用户删除成功");
    }

    /**
     * 用户更新信息前先对当前请求中的token进行验证
     * @param userInfoVo 更新用户信息
     * @return ResponseResult
     * @author zhang
     */
    @PutMapping("/update")
    public ResponseResult<String> updateUser(@RequestBody UserInfoVo userInfoVo) {
        String accessToken = (String) redisProcessor.get(RedisTransKey.getTokenKey(userInfoVo.getEmail()));
        Long userId = currentUserHolder.getUserId();
        User user = userService.selectUserByUserId(userId)
                .orElseThrow(() -> new BusinessException("用户不存在！"));
        if (!jwtProcessor.validateToken(accessToken, userId)) {
            return ResponseResult.fail(ErrorCode.TOKEN_ERROR.getCode(), "token验证失败");
        }
        BeanUtil.copyProperties(userInfoVo, user, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
        userService.updateUser(user);
        Map<String, Object> userMap = UserTransUtils.getUserMap(user);
        redisProcessor.set(RedisTransKey.getLoginKey(user.getEmail()), userMap, 7, TimeUnit.DAYS);
        redisProcessor.set(RedisTransKey.getTokenKey(user.getEmail()),userMap,7,TimeUnit.DAYS);
        redisProcessor.set(RedisTransKey.getRefreshTokenKey(user.getEmail()),userMap,7,TimeUnit.DAYS);
        return ResponseResult.success("用户信息更新成功");
    }

    /**
     * 修改用户密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return ResponseResult
     * @time 2024-09-13 16:53
     */

    @PutMapping("/update/password")
    public ResponseResult<String> updatePassword(String oldPassword, String newPassword) {
        if (StringUtil.isEmpty(oldPassword) || StringUtil.isEmpty(newPassword)) {
            return ResponseResult.fail("输入密码不能为空");
        }
        Long userId = currentUserHolder.getUserId();
        User user = userService.selectUserByUserId(userId)
                .orElseThrow(() -> new BusinessException("用户不存在！"));
        if (!SecurityUtils.checkPassword(oldPassword, user.getPassword())){
            return ResponseResult.fail("密码错误，请重新输入");
        }
        user.setPassword(SecurityUtils.encodePassword(newPassword));
        userService.updateUser(user);
        return ResponseResult.success("密码修改成功");
    }

    /**
     * 获取用户主页信息
     *
     * @return ResponseResult
     * @time 2024-09-13 15:29
     * @author zhang
     */
    @GetMapping("/home")
    public ResponseResult<UserInfoVo> getProfile() {
        Long userId = currentUserHolder.getUserId();
        User user = userService.selectUserByUserId(userId)
                .orElseThrow(() -> new BusinessException("用户不存在！"));
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtil.copyProperties(user, userInfoVo);
        return ResponseResult.success(userInfoVo);
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
        if (!validPageParams(params)){
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

}
