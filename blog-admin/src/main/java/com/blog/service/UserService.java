package com.blog.service;

import com.blog.authentication.CurrentUserHolder;
import com.blog.constant.Constant;
import com.blog.entity.User;
import com.blog.enums.ErrorCode;
import com.blog.exception.BusinessException;
import com.blog.mapper.UserMapper;
import com.blog.util.*;
import com.blog.util.bo.EmailCodeBo;
import com.blog.util.bo.LoginResponse;
import com.blog.util.redis.RedisTransKey;
import com.blog.util.redis.RedisProcessor;
import com.blog.vo.user.Loginer;
import com.blog.vo.user.Register;
import com.github.pagehelper.PageHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    private final RedisProcessor redisProcessor;

    private final JwtProcessor jwtProcessor;



    /**
     * 用户登录，登陆成功后返回accessToke，refreshToken，userId
     *
     * @Param loginer
     * @Return String
     * @Desription 用户登录
     */
    public LoginResponse userLogin(Loginer loginer) {
        String email = loginer.getEmail();
        String password = loginer.getPassword();
        //用户此时不为null，是否为null已经在selectUserByEmail判断
        User user = selectUserByEmail(email);
        boolean loginFlag = SecurityUtils.checkPassword(password, user.getPassword());
        if (!loginFlag) {
            log.error("密码错误，登陆失败，请重新输入");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误，登陆失败，请重新输入");
        }
        /*将用户信息存放在token中，时效为7天*/
        Map<String, Object> userMap = UserTransUtils.getUserMap(user);
        String accessToken = jwtProcessor.generateToken(userMap);
        String refreshToken = jwtProcessor.generateRefreshToken(userMap);
        redisProcessor.set(RedisTransKey.refreshTokenKey(email), refreshToken, 7, TimeUnit.DAYS);
        redisProcessor.set(RedisTransKey.tokenKey(email), accessToken, 7, TimeUnit.DAYS);
        redisProcessor.set(RedisTransKey.loginKey(email), email, 7, TimeUnit.DAYS);
        log.info("用户{}登陆成功", user.getAccount());
        LoginResponse loginResponse = new LoginResponse(accessToken,refreshToken);
        return loginResponse;
    }

    /**
     * 刷新accessToken信息，并生成新的refreshToken
     *
     * @Param refreshToken
     * @Return Map<String, Object>
     */
    public String refreshAccessToken(String refreshToken, Long userId) {
        //对刷新令牌进行验证，以防恶意利用其他用户refreshToken刷新
        if (!jwtProcessor.validateToken(refreshToken, userId)) {
            log.error("refreshToken验证失败,当前用户id：" + userId);
            throw new BusinessException(ErrorCode.TOKEN_ERROR, "refreshToken验证失败,当前用户id：" + userId);
        }
        User user = userMapper.selectByPrimaryKey(userId);
        //没过期生成一个新的token
        Map<String, Object> userMap = UserTransUtils.getUserMap(user);
        String accessToken = jwtProcessor.generateToken(userMap);
        String newRefreshToken = jwtProcessor.generateRefreshToken(userMap);
        redisProcessor.set(RedisTransKey.refreshTokenKey(user.getEmail()), newRefreshToken);
        redisProcessor.set(RedisTransKey.tokenKey(user.getEmail()), accessToken);
        log.info("token refresh success!");
        return accessToken;
    }


    /**
     * 注册新用户，并最后清理redis中的验证码信息
     *
     * @Description 注册新用户
     * @Param register
     * @Return User
     */
    public void userRegister(@Validated Register register) {
        String account = register.getAccount();
        log.info("开始注册新用户：" + account);
        String nickName = register.getNickName();
        String password = register.getPassword();
        String checkPassword = register.getCheckPassword();
        String email = register.getEmail();
        String phone = register.getPhone();
        String emailCode = register.getEmailCode().trim();
        if (userMapper.findByEmail(email) != null) {
            log.error("邮箱已注册，请重新输入：{}", email);
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, "邮箱已注册，请重新输入");
        }
        //验证两次输入密码是否一致
        if (!checkPassword.equals(password)) {
            log.error("两次输入密码不一致，注册失败：{}", account);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入密码不一致，请重新输入");
        }
        /*验证邮箱验证码*/
        EmailCodeBo emailCodeBo = Optional.ofNullable((EmailCodeBo) redisProcessor.get(RedisTransKey.getEmailKey(email))).orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "请先获取验证码"));
        if (!emailCodeBo.getCode().equals(emailCode)) {
            log.error("邮箱验证码输入错误，注册失败：{}", account);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请确认邮箱验证码是否正确");
        }
        if (!emailCodeBo.getEmail().equals(email)) {
            log.error("邮箱输入错误，注册失败：{}", account);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请确认邮箱验证码是否正确");
        }

        /*封装用户*/
        User user = new User();
        Long userId = SnowFlakeUtil.nextId();
        user.setUserId(userId);
        user.setAccount(account);
        user.setNickName(nickName);
        String encodePassword = SecurityUtils.encodePassword(password);
        user.setPassword(encodePassword);
        user.setEmail(email);
        user.setPhone(phone);
        String baseHomePageUrl = Constant.USER_BASE_PATH + userId;
        user.setWebsite(baseHomePageUrl);
        /*添加用户到数据库,并清理redis中存放的验证码*/
        userMapper.insertUser(user);
        redisProcessor.del(RedisTransKey.getEmailKey(email));
    }

    /**
     * @Description 根据用户邮箱查找用户
     * @Param email
     * @Return User
     */
    public User selectUserByEmail(String email) {
        User user = userMapper.findByEmail(email);
        if (user == null) {
            log.error("邮箱{}未注册，请注册", email);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "邮箱{}未注册，请注册", email);
        }
        return user;
    }

    /**
     * 根据用户昵称去查找用户信息
     *
     * @param nickName
     * @return
     */
    public List<User> selectUsersByNickName(String nickName, int pageNo, int pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<User> users = userMapper.selectUsersByNickName(nickName);
        if (users.isEmpty()) {
            log.error("未找到用户");
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "未找到用户");
        }
        return users;
    }

    /**
     * 查询所有用户
     * @return List<User>
     */
    public List<User> getUsers(int pageNo, int pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        return userMapper.selectUsers();
    }


    /**
     * @Description 根据用户id查找用户
     * @Param userId
     * @Return User
     */
    public Optional<User> selectUserByUserId(Long userId) {
        return Optional.ofNullable(userMapper.selectByPrimaryKey(userId));
    }

    /**
     * @param userId
     * @description 根据用户id删除用户
     */
    public void deleteUserById(Long userId) {
        userMapper.deleteByPrimaryKey(userId);
    }

    /**
     * 更新用户信息
     *
     * @param user
     */
    public void updateUser(User user) {
        userMapper.updateByPrimaryKeySelective(user);
    }


    public int getTotalCount() {
        return userMapper.selectTotalCount();
    }
}
