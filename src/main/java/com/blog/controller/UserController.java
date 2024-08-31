package com.blog.controller;

import com.blog.entity.User;
import com.blog.enums.ErrorCode;
import com.blog.exception.BusinessException;
import com.blog.mapper.UserMapper;
import com.blog.service.MailService;
import com.blog.service.UserService;

import com.blog.util.CodeUties;
import com.blog.util.SecurityUtils;
import com.blog.util.SnowFlakeUtil;
import com.blog.util.bo.HttpSessionBO;
import com.blog.vo.Loginer;
import com.blog.vo.Register;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.constraints.Email;
import java.util.Optional;


@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MailService mailService;
    @Autowired
    private HttpSession session;
    @Autowired
    private HttpSessionBO sessionBO;


    @PostMapping("/register")
    public void register(@RequestBody @Validated Register register){
        String account = register.getAccount();
        log.info("开始注册新用户："+account);
        String nickName = register.getNickName();
        String password = register.getPassword();
        String checkPassword = register.getCheckPassword();
        String email = register.getEmail();
        String phone = register.getPhone();
        String emailCode = register.getEmailCode().trim();

        if (userMapper.findByEmail(email)!=null){
            log.error("邮箱已注册，请重新输入：{}", email);
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS,"邮箱已注册，请重新输入");
        }

        /*验证两次输入密码是否一致*/
        if (!checkPassword.equals(password)){
            log.error("两次输入密码不一致，注册失败：{}", account);
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次输入密码不一致，请重新输入");
        }

        String codeSession = Optional.ofNullable(sessionBO.getCode().toString()).orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱输入为空"));
        String emailSession = Optional.ofNullable(sessionBO.getEmail().toString()).orElseThrow(()->new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱验证码输入为空"));
        if (!emailSession.equals(email)){
            log.error("邮箱输入错误，注册失败：{}", account);
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱输入有误");
        }
        if (!codeSession.equals(emailCode)) {
            log.error("邮箱验证码输入错误，注册失败：{}", account);
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请确认邮箱验证码是否正确");
        }
        /*封装用户*/
        User user = new User();
        Long userId = SnowFlakeUtil.getInstance().nextId();
        user.setUserId(userId);
        user.setAccount(account);
        user.setNickName(nickName);
        String BCPassword = SecurityUtils.encodePassword(password);
        user.setPassword(BCPassword);
        user.setEmail(email);
        user.setPhone(phone);
        /*添加用户到数据库*/
        userMapper.insertUser(user);
        log.info("用户 {} 添加成功",account);
//        userService.userRegister(register,sessionBO);
    }
    @GetMapping("/email_code")
    @ResponseBody
    public void getCode(String email){
        sessionBO = HttpSessionBO.getHttpSessionBO(session,email);
        mailService.sendCodeMailMessage(sessionBO);
    }

    @GetMapping("/login")
    @ResponseBody
    public void login(@Validated Loginer loginer){
        session.setAttribute("email", loginer.getEmail());
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
    public void verifyUser(@RequestParam String password) {
        String email = (String) session.getAttribute("email");
        userService.verifyUser(password,email);
    }
    @PutMapping("/profile/update")
    public void updateUser(@RequestBody User newUser) {
        Long id = (Long) session.getAttribute("id");
        newUser.setUserId(id);
        userService.updateUser(newUser);
    }




}
