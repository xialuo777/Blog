package com.blog.service;

import com.blog.authentication.CurrentUserHolder;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.mapper.UserMapper;
import com.blog.util.JwtProcessor;
import com.blog.util.SecurityUtils;
import com.blog.util.UserTransUtils;
import com.blog.util.bo.EmailCodeBo;
import com.blog.util.bo.LoginResponse;
import com.blog.util.redis.RedisTransKey;
import com.blog.util.redis.RedisProcessor;
import com.blog.vo.user.Loginer;
import com.blog.vo.user.Register;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper mockUserMapper;
    @Mock
    private CurrentUserHolder currentUserHolder;

    @Mock
    private RedisProcessor mockRedisProcessor;

    @Mock
    private JwtProcessor mockJwtService;
    @Mock
    private EmailCodeBo emailCodeBo;

    @InjectMocks
    private UserService mockUserService;
    @Mock
    private JwtProcessor mockJwtProcessor;

    @Test
    void UserLogin() {
        try (MockedStatic<SecurityUtils> mockedStatic = Mockito.mockStatic(SecurityUtils.class)) {
            final Loginer loginer = new Loginer("2436056388@qq.com", "password");

            final User user = new User();
            user.setUserId(0L);
            user.setAccount("account");
            user.setNickName("nickName");
            String password = SecurityUtils.encodePassword("password");
            user.setPassword(password);
            user.setEmail("2436056388@qq.com");
            user.setPhone("13781342354");
            when(mockUserMapper.findByEmail("2436056388@qq.com")).thenReturn(user);
            mockedStatic.when(() -> SecurityUtils.checkPassword("password", password)).thenReturn(true);

            Map<String, Object> userMap = UserTransUtils.getUserMap(user);
            when(mockJwtProcessor.generateToken(userMap)).thenReturn("value");
            when(mockJwtProcessor.generateRefreshToken(userMap)).thenReturn("refreshValue");

            LoginResponse loginResponse = mockUserService.userLogin(loginer);

            assertEquals("value", loginResponse.getAccessToken());
            assertEquals("refreshValue", loginResponse.getRefreshToken());

            verify(mockRedisProcessor).set(eq(RedisTransKey.refreshTokenKey("2436056388@qq.com")), eq("refreshValue"), eq(7L), eq(TimeUnit.DAYS));
            verify(mockRedisProcessor).set(eq(RedisTransKey.tokenKey("2436056388@qq.com")), eq("value"), eq(7L), eq(TimeUnit.DAYS));
            verify(mockRedisProcessor).set(eq(RedisTransKey.loginKey("2436056388@qq.com")), eq("2436056388@qq.com"), eq(7L), eq(TimeUnit.DAYS));
        }
    }

    @Test
    void UserLogin_With_Wrong_Password() {
        final Loginer loginer = new Loginer("2436056388@qq.com", "password1");
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
    void UserLogin_Without_User() {
        final Loginer loginer = new Loginer("2436056388@qq.com", "password");
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
        when(mockRedisProcessor.get(RedisTransKey.getEmailKey("2436056388@qq.com"))).thenReturn(emailCodeBo);

        final User user = new User();
        user.setUserId(0L);
        user.setAccount("account");
        user.setNickName("nickName");
        user.setPassword("password");
        user.setEmail("2436056388@qq.com");
        user.setPhone("13781342354");
        when(mockUserMapper.insertUser(any(User.class))).thenReturn(1);

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
        when(mockRedisProcessor.get(RedisTransKey.getEmailKey("2436056388@qq.com"))).thenReturn(emailCodeBo);

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
        when(mockRedisProcessor.get(RedisTransKey.getEmailKey("2436056388@qq.com"))).thenReturn(emailCodeBo);

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

        when(mockUserMapper.findByEmail("2436056388@qq.com")).thenReturn(expectedResult);

        final User result = mockUserService.selectUserByEmail("2436056388@qq.com");
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void SelectUserByEmail_Without_User() {
        when(mockUserMapper.findByEmail("2436056388@qq.com")).thenReturn(null);

        assertThatThrownBy(() -> mockUserService.selectUserByEmail("2436056388@qq.com")).isInstanceOf(BusinessException.class);
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


        when(currentUserHolder.getUserId()).thenReturn(0L);
        when(mockUserMapper.updateByPrimaryKeySelective(any(User.class))).thenReturn(1);

        mockUserService.updateUser(user);

        verify(currentUserHolder).getUserId();
        verify(mockUserMapper).updateByPrimaryKeySelective(any(User.class));

    }
}
