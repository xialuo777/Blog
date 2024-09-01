package com.blog.service;

import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.mapper.UserMapper;
import com.blog.util.SecurityUtils;
import com.blog.util.bo.EmailCodeBo;
import com.blog.util.redis.RedisTransKey;
import com.blog.util.redis.RedisUtils;
import com.blog.vo.Loginer;
import com.blog.vo.Register;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper mockUserMapper;

    @Mock
    private RedisUtils mockRedisUtils;

    @Mock
    private JwtService mockJwtService;
    @Mock
    private EmailCodeBo emailCodeBo;

    @InjectMocks
    private UserService mockUserService;


    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mockUserService, "userMapper", mockUserMapper);
        ReflectionTestUtils.setField(mockUserService, "redisUtils", mockRedisUtils);
        ReflectionTestUtils.setField(mockUserService, "jwtService", mockJwtService);
    }

    @Test
    void UserLogin() {
        final Loginer loginer = new Loginer("2436056388@qq.com", "password");
        when(mockRedisUtils.hasKey(RedisTransKey.getLoginKey("2436056388@qq.com"))).thenReturn(false);

        final User user = new User();
        user.setUserId(0L);
        user.setAccount("account");
        user.setNickName("nickName");
        String password =SecurityUtils.encodePassword("password");
        user.setPassword(password);
        user.setEmail("2436056388@qq.com");
        user.setPhone("13781342354");
        when(mockUserMapper.findByEmail("2436056388@qq.com")).thenReturn(user);

        final User user1 = new User();
        user1.setUserId(0L);
        user1.setAccount("account");
        user1.setNickName("nickName");
        user1.setPassword(password);
        user1.setEmail("2436056388@qq.com");
        user1.setPhone("13781342354");
        when(mockJwtService.generateToken(user1)).thenReturn("value");

        mockUserService.userLogin(loginer);

        verify(mockRedisUtils).set(eq(RedisTransKey.setTokenKey("2436056388@qq.com")), eq("value"), eq(7L), eq(TimeUnit.DAYS));
        verify(mockRedisUtils).set(eq(RedisTransKey.setLoginKey("2436056388@qq.com")), eq("2436056388@qq.com"), eq(7L), eq(TimeUnit.DAYS));
    }

    @Test
    void UserLogin_With_Wrong_Password() {
        final Loginer loginer = new Loginer("2436056388@qq.com", "password1");
        when(mockRedisUtils.hasKey(RedisTransKey.getLoginKey("2436056388@qq.com"))).thenReturn(false);

        final User user = new User();
        user.setUserId(0L);
        user.setAccount("account");
        user.setNickName("nickName");
        String password =SecurityUtils.encodePassword("password");
        user.setPassword(password);
        user.setEmail("2436056388@qq.com");
        user.setPhone("13781342354");
        when(mockUserMapper.findByEmail("2436056388@qq.com")).thenReturn(user);

        assertThrows(BusinessException.class, () -> mockUserService.userLogin(loginer));
    }

    @Test
    void UserLogin__LoginError_AlreadyLoggedIn() {

        final Loginer loginer = new Loginer("2436056388@qq.com", "password");
        when(mockRedisUtils.hasKey(RedisTransKey.getLoginKey("2436056388@qq.com"))).thenReturn(true);

        assertThatThrownBy(() -> mockUserService.userLogin(loginer)).isInstanceOf(BusinessException.class);
    }

    @Test
    void UserLogin_Without_User() {
        final Loginer loginer = new Loginer("2436056388@qq.com", "password");
        when(mockRedisUtils.hasKey(RedisTransKey.getLoginKey("2436056388@qq.com"))).thenReturn(false);
        when(mockUserMapper.findByEmail("2436056388@qq.com")).thenReturn(null);

        assertThatThrownBy(() -> mockUserService.userLogin(loginer)).isInstanceOf(BusinessException.class);
    }

    @Test
    void UserRegister() {
        final Register register = new Register();
        register.setAccount("account");
        register.setNickName("nickName");
        register.setPassword("password");
        register.setCheckPassword("password");
        register.setEmail("2436056388@qq.com");
        register.setPhone("13781342354");
        register.setEmailCode("tested");
        when(mockUserMapper.findByEmail("2436056388@qq.com")).thenReturn(null);
        emailCodeBo = new EmailCodeBo();
        emailCodeBo.setEmail("2436056388@qq.com");
        emailCodeBo.setCode("tested");
        when(mockRedisUtils.get(RedisTransKey.getEmailKey("2436056388@qq.com"))).thenReturn(emailCodeBo);

        final User user = new User();
        user.setUserId(0L);
        user.setAccount("account");
        user.setNickName("nickName");
        user.setPassword("password");
        user.setEmail("2436056388@qq.com");
        user.setPhone("13781342354");
        doNothing().when(mockUserMapper).insertUser(any(User.class));

        mockUserService.userRegister(register);

        verify(mockUserMapper, times(1)).insertUser(any(User.class));


    }
    @Test
    void UserRegister_With_Wrong_Email() {
        final Register register = new Register();
        register.setAccount("account");
        register.setNickName("nickName");
        register.setPassword("password");
        register.setCheckPassword("password");
        register.setEmail("2436056388@qq.com");
        register.setPhone("13781342354");
        register.setEmailCode("tested");
        when(mockUserMapper.findByEmail("2436056388@qq.com")).thenReturn(null);
        emailCodeBo = new EmailCodeBo();
        emailCodeBo.setEmail("2436056387@qq.com");
        emailCodeBo.setCode("tested");
        when(mockRedisUtils.get(RedisTransKey.getEmailKey("2436056388@qq.com"))).thenReturn(emailCodeBo);

        assertThrows(BusinessException.class, () -> mockUserService.userRegister(register));

    }

    @Test
    void UserRegister_With_Wrong_Email_Code() {
        final Register register = new Register();
        register.setAccount("account");
        register.setNickName("nickName");
        register.setPassword("password");
        register.setCheckPassword("password");
        register.setEmail("2436056388@qq.com");
        register.setPhone("13781342354");
        register.setEmailCode("tested");
        when(mockUserMapper.findByEmail("2436056388@qq.com")).thenReturn(null);
        emailCodeBo = new EmailCodeBo();
        emailCodeBo.setEmail("2436056388@qq.com");
        emailCodeBo.setCode("testes");
        when(mockRedisUtils.get(RedisTransKey.getEmailKey("2436056388@qq.com"))).thenReturn(emailCodeBo);

        assertThrows(BusinessException.class, () -> mockUserService.userRegister(register));

    }
    @Test
    void UserRegister_With_Wrong_Check_Password() {
        final Register register = new Register();
        register.setAccount("account");
        register.setNickName("nickName");
        register.setPassword("password");
        register.setCheckPassword("password1");
        register.setEmail("2436056388@qq.com");
        register.setPhone("13781342354");
        register.setEmailCode("tested");
        assertThrows(BusinessException.class, () -> mockUserService.userRegister(register));

    }
    @Test
    void UserRegister_Already_Registered() {
        final Register register = new Register();
        register.setAccount("account");
        register.setNickName("nickName");
        register.setPassword("password");
        register.setCheckPassword("password");
        register.setEmail("2436056388@qq.com");
        register.setPhone("13781342354");
        register.setEmailCode("tested");

        final User user = new User();
        user.setUserId(0L);
        user.setAccount("account");
        user.setNickName("nickName");
        user.setPassword("password");
        user.setEmail("2436056388@qq.com");
        user.setPhone("13781342354");
        when(mockUserMapper.findByEmail("2436056388@qq.com")).thenReturn(user);

        assertThatThrownBy(() -> mockUserService.userRegister(register)).isInstanceOf(BusinessException.class);
    }


    @Test
    void SelectUserByEmail() {
        final User expectedResult = new User();
        expectedResult.setUserId(0L);
        expectedResult.setAccount("account");
        expectedResult.setNickName("nickName");
        expectedResult.setPassword("password");
        expectedResult.setEmail("2436056388@qq.com");
        expectedResult.setPhone("13781342354");

        final User user = new User();
        user.setUserId(0L);
        user.setAccount("account");
        user.setNickName("nickName");
        user.setPassword("password");
        user.setEmail("2436056388@qq.com");
        user.setPhone("13781342354");
        when(mockUserMapper.findByEmail("2436056388@qq.com")).thenReturn(user);

        final User result = mockUserService.selectUserByEmail("2436056388@qq.com");
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void SelectUserByEmail_Without_User() {
        when(mockUserMapper.findByEmail("2436056388@qq.com")).thenReturn(null);

        assertThatThrownBy(() -> mockUserService.selectUserByEmail("2436056388@qq.com")).isInstanceOf(BusinessException.class);
    }

    @Test
    void DeleteUserByEmail() {
        final User user = new User();
        user.setUserId(0L);
        user.setAccount("account");
        user.setNickName("nickName");
        user.setPassword("password");
        user.setEmail("2436056388@qq.com");
        user.setPhone("13781342354");
        when(mockUserMapper.findByEmail("2436056388@qq.com")).thenReturn(user);

        mockUserService.deleteUserByEmail("2436056388@qq.com");

        verify(mockUserMapper).deleteByEmail("2436056388@qq.com");
    }

    @Test
    void testDeleteUserByEmail_Without_User() {
        when(mockUserMapper.findByEmail("2436056388@qq.com")).thenReturn(null);

        assertThatThrownBy(() -> mockUserService.deleteUserByEmail("2436056388@qq.com")).isInstanceOf(BusinessException.class);
    }

    @Test
    void VerifyUser() {
        final User user = new User();
        user.setUserId(0L);
        user.setAccount("account");
        user.setNickName("nickName");
        user.setPassword(SecurityUtils.encodePassword("password"));
        user.setEmail("2436056388@qq.com");
        user.setPhone("13781342354");
        when(mockUserMapper.findByEmail("2436056388@qq.com")).thenReturn(user);

        mockUserService.verifyUser("password", "2436056388@qq.com");

    }
    @Test
    void VerifyUser_With_Wrong_Password() {
        final User user = new User();
        user.setUserId(0L);
        user.setAccount("account");
        user.setNickName("nickName");
        user.setPassword(SecurityUtils.encodePassword("password1"));
        user.setEmail("2436056388@qq.com");
        user.setPhone("13781342354");
        when(mockUserMapper.findByEmail("2436056388@qq.com")).thenReturn(user);

        assertThrows(BusinessException.class, ()->mockUserService.verifyUser("password", "2436056388@qq.com"));


    }


    @Test
    void testUpdateUser() {
        final User user = new User();
        user.setUserId(0L);
        user.setAccount("account");
        user.setNickName("nickName");
        user.setPassword("password");
        user.setEmail("email");
        user.setPhone("phone");
        mockUserService.updateUser(user);

        final User user1 = new User();
        user1.setUserId(0L);
        user1.setAccount("account");
        user1.setNickName("nickName");
        user1.setPassword("password");
        user1.setEmail("email");
        user1.setPhone("phone");
        verify(mockUserMapper).updateUser(user1);
    }
}
