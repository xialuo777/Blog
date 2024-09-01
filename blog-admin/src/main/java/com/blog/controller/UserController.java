package com.blog.controller;
import com.blog.entity.User;

import com.blog.service.JwtService;
import com.blog.service.MailService;
import com.blog.service.UserService;
import com.blog.vo.Loginer;
import com.blog.vo.Register;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;


@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final MailService mailService;
    private final JwtService jwtService;

    private final HttpSession session;


    @PostMapping("/register")
    public ResponseEntity register(@RequestBody @Validated Register register) {
        User user = userService.userRegister(register);
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/email_code")
    @ResponseBody
    public void getCode(@RequestParam @Email String email) {
        mailService.getEmailCode(email);
    }

    @GetMapping("/login")
    @ResponseBody
    public void login(@Validated Loginer loginer) {
        userService.userLogin(loginer);

    }

    @DeleteMapping("/delete")
    public void delete(@RequestParam String email) {
        userService.deleteUserByEmail(email);
    }

    @GetMapping("/profile")
    public String profile() {
        String email = (String) session.getAttribute("email");
        User user = userService.selectUserByEmail(email);
        session.setAttribute("path", "profile");
        session.setAttribute("account", user.getAccount());
        session.setAttribute("nickName", user.getNickName());
        session.setAttribute("id", user.getUserId());
        return "users/profile";
    }

    @GetMapping("/profile/verify")
    public void verifyUser(@RequestParam @NotBlank String password) {
        String email = (String) session.getAttribute("email");
        userService.verifyUser(password, email);
    }

    @PutMapping("/profile/update")
    public void updateUser(@RequestBody User newUser) {
        Long id = (Long) session.getAttribute("id");
        newUser.setUserId(id);
        userService.updateUser(newUser);
    }


}
