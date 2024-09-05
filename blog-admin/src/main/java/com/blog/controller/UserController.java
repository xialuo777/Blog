package com.blog.controller;

import com.blog.authentication.CurrentUserHolder;
import com.blog.entity.User;

import com.blog.exception.BusinessException;
import com.blog.mapper.UserMapper;
import com.blog.service.MailService;
import com.blog.service.UserService;
import com.blog.util.JwtProcessor;
import com.blog.util.bo.LoginResponse;
import com.blog.util.redis.RedisProcessor;
import com.blog.util.redis.RedisTransKey;
import com.blog.vo.user.Loginer;
import com.blog.vo.user.Register;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Email;
import java.util.ArrayList;
import java.util.List;


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
    public ResponseEntity<String> register(@RequestBody @Validated Register register) {
        userService.userRegister(register);
        return ResponseEntity.ok("用户注册成功");
    }

    @GetMapping("/email_code")
    @ResponseBody
    public void getCode(@RequestParam @Email String email) {
        mailService.getEmailCode(email);
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<LoginResponse> login(@Validated @RequestBody Loginer loginer) {
        //用户登陆后返回给前端accessToken和refreshToken
        LoginResponse loginResponse = userService.userLogin(loginer);
        return ResponseEntity.ok(loginResponse);
    }

    /**
     * 刷新token
     *
     * @return accessToken
     */
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(@RequestParam Long userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user.getStatus() == 0){
            log.error("该用户处于异常状态，无法执行下一步操作");
            throw new BusinessException("该用户处于异常状态，无法执行下一步操作");
        }
        String refreshToken = (String) redisProcessor.get(RedisTransKey.getRefreshTokenKey(user.getEmail()));
        //返回给前端刷新后的accessToken,同时也会产生新的refreshToken,以防refreshToken过期
        String accessToken = userService.refreshAccessToken(refreshToken, userId);
        return ResponseEntity.ok(accessToken);
    }

    /***
     * 用户退出登陆时，需要删除token信息
     * @param request
     * @return ResponseEntity
     */
    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String accessToken = request.getHeader("accessToken");
        Long userId = currentUserHolder.getUserId();
        if (!jwtProcessor.validateToken(accessToken, userId)) {
            return ResponseEntity.ok("token验证失败");
        }
        User user = userMapper.selectByPrimaryKey(userId);
        redisProcessor.del(RedisTransKey.getRefreshTokenKey(user.getEmail()));
        redisProcessor.del(RedisTransKey.getLoginKey(user.getEmail()));
        redisProcessor.del(RedisTransKey.getTokenKey(user.getEmail()));
        log.info("用户退出登陆成功");
        return ResponseEntity.ok("用户退出登陆成功");
    }

    /**
     * 用户删除登录会先对当前请求中的token进行验证
     * @param request
     * @return
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(HttpServletRequest request) {
        String accessToken = request.getHeader("accessToken");
        Long userId = currentUserHolder.getUserId();
        if (!jwtProcessor.validateToken(accessToken, userId)) {
            return ResponseEntity.ok("token验证失败");
        }
        userService.deleteUserById(userId);
        return ResponseEntity.ok("用户删除成功");
    }

    /**
     * 用户更新信息前先对当前请求中的token进行验证
     * @param user
     * @param request
     * @return
     */
    @PutMapping("/update")
    public ResponseEntity<String> updateUser(@RequestBody User user, HttpServletRequest request) {
        String accessToken = request.getHeader("accessToken");
        Long userId = currentUserHolder.getUserId();
        if (!jwtProcessor.validateToken(accessToken, userId)) {
            return ResponseEntity.ok("token验证失败");
        }
        userService.updateUser(user);
        return ResponseEntity.ok("用户信息更新成功");
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile() {
        Long userId = currentUserHolder.getUserId();
        User user = userService.selectUserByUserId(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{email}")
    public ResponseEntity<String> getUserWebByEmail(@PathVariable String email) {
        User user = userService.selectUserByEmail(email);
        String website = user.getWebsite();
        return ResponseEntity.ok(website);
    }

    @GetMapping("/{nickName}")
    public ResponseEntity<List<String>> getUserWebByNickName(@PathVariable String nickName, @RequestParam int pageNo, @RequestParam int pageSize) {
        List<User> users = userService.selectUsersByNickName(nickName,pageNo,pageSize);
        List<String> result = new ArrayList<>();
        for (User user : users) {
            result.add(user.getWebsite());
        }
        return ResponseEntity.ok(result);
    }


}
