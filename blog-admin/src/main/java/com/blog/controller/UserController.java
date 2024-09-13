package com.blog.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.blog.authentication.CurrentUserHolder;
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
import com.blog.util.redis.RedisProcessor;
import com.blog.util.redis.RedisTransKey;
import com.blog.vo.user.UserInfoVo;
import com.blog.vo.user.Loginer;
import com.blog.vo.user.Register;
import com.github.pagehelper.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Email;
import java.util.Map;
import java.util.concurrent.TimeUnit;


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
     *
     * @param register
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
     * @param email
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
     * @param loginer
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
     * @param refreshToken
     * @return ResponseResult
     * @author zhang
     */
    @PostMapping("/refresh")
    public ResponseResult<String> refreshToken(String refreshToken) {
        Map<String, Object> userMap = jwtProcessor.extractUserMap(refreshToken);
        Long userId = (Long) userMap.get("userId");
        User user = userMapper.selectByPrimaryKey(userId);
        if (user.getStatus() == 0) {
            log.error("该用户处于异常状态，无法执行下一步操作");
            throw new BusinessException("该用户处于异常状态，无法执行下一步操作");
        }
        //返回给前端刷新后的accessToken,同时也会产生新的refreshToken,以防refreshToken过期
        String accessToken = userService.refreshAccessToken(refreshToken, userId);
        return ResponseResult.success(accessToken);
    }

    /***
     * 用户退出登陆时，需要删除token信息
     * @param request
     * @return ResponseEntity
     * @Author zhang
     */
    @GetMapping("/logout")
    public ResponseResult<String> logout(HttpServletRequest request) {
        String accessToken = request.getHeader("accessToken");
        Long userId = currentUserHolder.getUserId();
        if (!jwtProcessor.validateToken(accessToken, userId)) {
            return ResponseResult.fail(ErrorCode.TOKEN_ERROR.getCode(), "token验证失败");
        }
        User user = userMapper.selectByPrimaryKey(userId);
        redisProcessor.del(RedisTransKey.getRefreshTokenKey(user.getEmail()));
        redisProcessor.del(RedisTransKey.getLoginKey(user.getEmail()));
        redisProcessor.del(RedisTransKey.getTokenKey(user.getEmail()));
        log.info("用户退出登陆成功");
        return ResponseResult.success("用户退出登陆成功");
    }

    /**
     * 用户删除登录会先对当前请求中的token进行验证
     *
     * @param request
     * @return ResponseResult
     * @author zhang
     */
    @DeleteMapping("/delete")
    public ResponseResult<String> delete(HttpServletRequest request) {
        String accessToken = request.getHeader("accessToken");
        Long userId = currentUserHolder.getUserId();
        userService.selectUserByUserId(userId)
                .orElseThrow(() -> new BusinessException("用户不存在！"));
        if (!jwtProcessor.validateToken(accessToken, userId)) {
            return ResponseResult.fail(ErrorCode.TOKEN_ERROR.getCode(), "token验证失败");
        }
        userService.deleteUserById(userId);
        return ResponseResult.success("用户删除成功");
    }

    /**
     * 用户更新信息前先对当前请求中的token进行验证
     *
     * @param userInfoVo
     * @return ResponseResult
     * @author zhang
     */
    @PutMapping("/update")
    public ResponseResult<String> updateUser(@RequestBody UserInfoVo userInfoVo) {
        String accessToken = (String) redisProcessor.get(RedisTransKey.getTokenKey(userInfoVo.getEmail()));
        Long userId = currentUserHolder.getUserId();
        if (!jwtProcessor.validateToken(accessToken, userId)) {
            return ResponseResult.fail(ErrorCode.TOKEN_ERROR.getCode(), "token验证失败");
        }
        User user = userService.selectUserByUserId(userId)
                .orElseThrow(() -> new BusinessException("用户不存在！"));
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
     * @param oldPassword
     * @param newPassword
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


}
