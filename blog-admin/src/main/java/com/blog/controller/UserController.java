package com.blog.controller;

import com.blog.authentication.CurrentUserHolder;
import com.blog.entity.User;

import com.blog.mapper.UserMapper;
import com.blog.service.MailService;
import com.blog.service.UserService;
import com.blog.util.JwtProcessor;
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
import java.util.Currency;


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
    public ResponseEntity<String> login(@Validated @RequestBody Loginer loginer) {
        //用户登陆后返回给前端accessToken和refreshToken
        String accessToken = userService.userLogin(loginer);
        return ResponseEntity.ok(accessToken);
    }

    /**
     * 刷新token
     * @return accessToken
     */
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken() {
        Long userId = CurrentUserHolder.getUserId();
        User user = userMapper.selectByPrimaryKey(userId);
        String refreshToken = (String) redisProcessor.get(RedisTransKey.getRefreshTokenKey(user.getEmail()));
        String accessToken = userService.refreshAccessToken(refreshToken);
        //返回给前端刷新后的accessToken,同时也会产生新的refreshToken,以防refreshToken过期
        return ResponseEntity.ok(accessToken);
    }

    /***
     * 用户退出登陆时，需要删除token信息
     * @param request
     * @return
     */
    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String accessToken = request.getHeader("accessToken");
        Long userId = jwtProcessor.extractUserId(accessToken);
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

    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(HttpServletRequest request) {
        String accessToken = request.getHeader("accessToken");
        Long userId = Long.valueOf(request.getHeader("userId"));
        if (!jwtProcessor.validateToken(accessToken, userId)) {
            return ResponseEntity.ok("token验证失败");
        }
        userService.deleteUserById(userId);
        return ResponseEntity.ok("用户删除成功");
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateUser(@RequestBody User user) {
        userService.updateUser(user);
        return ResponseEntity.ok("用户信息更新成功");
    }


}
