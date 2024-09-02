package com.blog.controller;
import com.blog.entity.User;

import com.blog.authentication.RequestAuthentication;
import com.blog.mapper.UserMapper;
import com.blog.service.MailService;
import com.blog.service.UserService;
import com.blog.util.redis.RedisProcessor;
import com.blog.util.redis.RedisTransKey;
import com.blog.vo.Loginer;
import com.blog.vo.Register;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Email;

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



    @PostMapping("/register")
    public ResponseEntity register(@RequestBody @Validated Register register) {
        User user = userService.userRegister(register);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/email_code")
    @ResponseBody
    public void getCode(@RequestParam @Email String email) {
        mailService.getEmailCode(email);
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity login(@Validated @RequestBody Loginer loginer) {
        //用户登陆后返回给前端accessToken和refreshToken
        Map<String, Object> tokenMap = userService.userLogin(loginer);
        return ResponseEntity.ok(tokenMap);
    }

    /**
     * 当短时间的accessToken过期后，前端需要通过refreshToke访问后端，
     * 并生成新的accessToken返回给前端
     * @param request
     * @return
     */
    @PostMapping("/token")
    public ResponseEntity refreshToken(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getHeader("userId"));
        Map<String, Object> tokenMap = userService.refreshAccessToken(userId);
        //返回给前端刷新后的accessToken,同时也会产生新的refreshToken,以防refreshToken过期
        return ResponseEntity.ok(tokenMap);
    }

    /***
     * 用户退出登陆时，需要删除token信息
     * @param request
     * @return
     */
    @GetMapping("/logout")
    public ResponseEntity logout(HttpServletRequest request) {
        String token = request.getHeader("accessToken");
        Long userId = Long.valueOf(request.getHeader("userId"));
        User user = userMapper.selectByPrimaryKey(userId);
        RedisTransKey.getTokenKey(token);
        redisProcessor.del(RedisTransKey.getRefreshTokenKey(user.getEmail()),RedisTransKey.getTokenKey(user.getEmail()),RedisTransKey.getLoginKey(user.getEmail()));
        return ResponseEntity.ok("退出成功");
    }

    @DeleteMapping("/delete")
    public ResponseEntity delete(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getHeader("userId"));
        userService.deleteUserById(userId);
        return ResponseEntity.ok("用户删除成功");
    }

    @PutMapping("/update")
    public ResponseEntity updateUser(@RequestBody User user) {
        userService.updateUser(user);
        return ResponseEntity.ok("用户信息更新成功");
    }


}
