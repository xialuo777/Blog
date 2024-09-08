package com.blog.controller;

import cn.hutool.core.bean.BeanUtil;
import com.blog.authentication.CurrentUserHolder;
import com.blog.dto.PageResult;
import com.blog.entity.User;

import com.blog.enums.ErrorCode;
import com.blog.exception.BusinessException;
import com.blog.exception.ResponseResult;
import com.blog.mapper.UserMapper;
import com.blog.service.MailService;
import com.blog.service.UserService;
import com.blog.util.JwtProcessor;
import com.blog.util.bo.LoginResponse;
import com.blog.util.redis.RedisProcessor;
import com.blog.util.redis.RedisTransKey;
import com.blog.vo.user.UserInfoVo;
import com.blog.vo.user.UserVo;
import com.blog.vo.user.Loginer;
import com.blog.vo.user.Register;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Email;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final MailService mailService;
    private final RedisProcessor redisProcessor;
    private final UserMapper userMapper;
    private final JwtProcessor jwtProcessor;
    private final CurrentUserHolder currentUserHolder;


    @PostMapping("/register")
    public ResponseResult<String> register(@RequestBody @Validated Register register) {
        userService.userRegister(register);
        return ResponseResult.success("用户注册成功");
    }

    @GetMapping("/getCode")
    @ResponseBody
    public ResponseResult<String> getCode(@RequestParam @Email String email) {
        mailService.getEmailCode(email);
        return ResponseResult.success("验证码发送成功");
    }

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
     * @return accessToken
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
     * @return
     */
    @DeleteMapping("/delete")
    public ResponseResult<String> delete(HttpServletRequest request) {
        String accessToken = request.getHeader("accessToken");
        Long userId = currentUserHolder.getUserId();
        if (!jwtProcessor.validateToken(accessToken, userId)) {
            return ResponseResult.fail(ErrorCode.TOKEN_ERROR.getCode(), "token验证失败");
        }
        userService.deleteUserById(userId);
        return ResponseResult.success("用户删除成功");
    }

    /**
     * 用户更新信息前先对当前请求中的token进行验证
     *
     * @param user
     * @param request
     * @return
     */
    @PutMapping("/update")
    public ResponseResult<String> updateUser(@RequestBody User user, HttpServletRequest request) {
        String accessToken = request.getHeader("accessToken");
        Long userId = currentUserHolder.getUserId();
        if (!jwtProcessor.validateToken(accessToken, userId)) {
            return ResponseResult.fail(ErrorCode.TOKEN_ERROR.getCode(), "token验证失败");
        }
        userService.updateUser(user);
        return ResponseResult.success("用户信息更新成功");
    }

    @GetMapping("/home")
    public ResponseResult<UserInfoVo> getProfile() {
        Long userId = currentUserHolder.getUserId();
        User user = userService.selectUserByUserId(userId);
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtil.copyProperties(user,userInfoVo);
        return ResponseResult.success(userInfoVo);
    }

    @GetMapping("/{email}")
    public ResponseResult<String> getUserWebByEmail(@PathVariable String email) {
        User user = userService.selectUserByEmail(email);
        String website = user.getWebsite();
        return ResponseResult.success(website);
    }

    @GetMapping("/{nickName}")
    public ResponseResult<PageResult<UserVo>> getUserWebByNickName(@PathVariable String nickName, @RequestParam int pageNo, @RequestParam int pageSize) {
        List<User> users = userService.selectUsersByNickName(nickName, pageNo, pageSize);
        List<UserVo> result = new ArrayList<>();
        for (User user : users) {
            UserVo userVo = new UserVo();
            BeanUtil.copyProperties(user,userVo);
            result.add(userVo);
        }
        int totalCount = userService.getTotalCount();
        PageResult<UserVo> pageResult = new PageResult<>(result, totalCount);
        return ResponseResult.success(pageResult);
    }

    @GetMapping("/getUsers")
    public ResponseResult<PageResult<UserVo>> getUsers(@RequestParam int pageNo, @RequestParam int pageSize) {
        List<User> users = userService.getUsers(pageNo, pageSize);
        List<UserVo> result = new ArrayList<>(users.size());
        for (User user : users) {
            UserVo userVo = new UserVo();
            BeanUtil.copyProperties(user,userVo);
            result.add(userVo);
        }
        int totalCount = userService.getTotalCount();
        PageResult<UserVo> pageResult = new PageResult<>(result, totalCount);
        return ResponseResult.success(pageResult);
    }


}
