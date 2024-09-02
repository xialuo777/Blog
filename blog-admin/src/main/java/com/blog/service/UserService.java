package com.blog.service;

import com.blog.entity.User;
import com.blog.enums.ErrorCode;
import com.blog.exception.BusinessException;
import com.blog.mapper.UserMapper;
import com.blog.util.JwtProcessor;
import com.blog.util.SecurityUtils;
import com.blog.util.SnowFlakeUtil;
import com.blog.util.bo.EmailCodeBo;
import com.blog.util.redis.RedisTransKey;
import com.blog.util.redis.RedisProcessor;
import com.blog.vo.Loginer;
import com.blog.vo.Register;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
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
     * @param loginer
     * @Descriptionn 用户登录
     * @Return Map<String,Object>
     */
    public Map<String, Object> userLogin(Loginer loginer) {
        String email = loginer.getEmail();
        String password = loginer.getPassword();
        //验证是否存在旧的refresh token，存在即删除
        if (redisProcessor.hasKey(RedisTransKey.getRefreshTokenKey(email))) {
            redisProcessor.del(RedisTransKey.getRefreshTokenKey(email));
        }
        //判断用户是否已经登陆
        if (redisProcessor.hasKey(RedisTransKey.getLoginKey(email))) {
            log.error("用户已登录！");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已登录！");
        }
        //用户此时不为null，是否为null已经在selectUserByEmail判断
        User user = selectUserByEmail(email);
        boolean loginFlag = SecurityUtils.checkPassword(password, user.getPassword());
        if (!loginFlag) {
            log.error("密码错误，登陆失败，请重新输入");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误,请重新输入");
        }
        /*将用户信息存放在token中，时效为7天*/
        String token = jwtProcessor.generateToken(user.getUserId());
        String refreshToken = jwtProcessor.generateRefreshToken(user.getUserId());
        redisProcessor.set(RedisTransKey.tokenKey(email), token, 7, TimeUnit.DAYS);
        redisProcessor.set(RedisTransKey.refreshTokenKey(email), refreshToken, 7, TimeUnit.DAYS);
        redisProcessor.set(RedisTransKey.loginKey(email), email, 7, TimeUnit.DAYS);
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", token);
        result.put("refreshToken", refreshToken);
        result.put("userId", user.getUserId());
        log.info("用户{}登陆成功", user.getAccount());
        return result;
    }

    /**
     * 刷新accessToken信息，并生成新的refreshToken
     * @param userId
     * @return Map<String,Object>
     */
    public Map<String, Object> refreshAccessToken(Long userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        //从redis中拿到refreshToken，判断其是否过期
        String refreshTokenRedis = (String) redisProcessor.get(RedisTransKey.getRefreshTokenKey(user.getEmail()));
        if (refreshTokenRedis == null) {
            log.error("refreshToken已过期，请重新登录");
            throw new BusinessException(ErrorCode.TOKEN_ERROR, "token已过期！");
        }
        //没过期生成一个新的token
        String accessToken = jwtProcessor.generateToken(userId);
        String newRefreshToken = jwtProcessor.generateRefreshToken(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", newRefreshToken);
        result.put("userId", userId);
        return result;
    }

    /**
     * 注册新用户，并最后清理redis中的验证码信息
     * @Description 注册新用户
     * @Param register
     * @Return User
     */
    public User userRegister(@Validated Register register) {
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
        Long userId = SnowFlakeUtil.getInstance().nextId();
        user.setUserId(userId);
        user.setAccount(account);
        user.setNickName(nickName);
        String BCPassword = SecurityUtils.encodePassword(password);
        user.setPassword(BCPassword);
        user.setEmail(email);
        user.setPhone(phone);
        /*添加用户到数据库,并清理redis中存放的验证码*/
        userMapper.insertUser(user);
        redisProcessor.del(RedisTransKey.getEmailKey(email));
        log.info("用户 {} 添加成功", account);
        return user;
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
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "邮箱未注册，请注册");
        }
        log.info("查找用户成功");
        return user;
    }

    /**
     * @param userId
     * @Description 根据用户邮箱删除用户
     */
    public void deleteUserById(Long userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            log.error("邮箱未注册，无需删除");
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "邮箱未注册，无需删除");
        }
        userMapper.deleteByPrimaryKey(userId);
        log.info("用户{}删除成功", user.getEmail());
    }

    /**
     * 更新用户信息
     * @param user
     */
    public void updateUser(User user) {
        userMapper.updateByPrimaryKeySelective(user);
        log.info("用户修改成功");
    }
}
