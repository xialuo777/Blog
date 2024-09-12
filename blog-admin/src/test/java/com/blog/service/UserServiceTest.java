package com.blog.service;

import com.blog.authentication.CurrentUserHolder;
import com.blog.entity.User;
import com.blog.enums.ErrorCode;
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
import com.github.pagehelper.PageHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
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
    void refreshAccessToken_SuccessfullyRefreshesTokens() {
        try (MockedStatic<UserTransUtils> userTransUtilsMockedStatic = Mockito.mockStatic(UserTransUtils.class)){
            String refreshToken = "validRefreshToken";
            String accessToken = "newAccessToken";
            String newRefreshToken = "newRefreshToken";
            Long userId = 1L;

            when(mockJwtProcessor.validateToken(refreshToken, userId)).thenReturn(true);
            User user = new User();
            user.setUserId(userId);
            user.setEmail("user@example.com");
            user.setNickName("nickname");
            user.setAccount("account");
            when(mockUserMapper.selectByPrimaryKey(userId)).thenReturn(user);
            when(mockJwtProcessor.validateToken(refreshToken, userId)).thenReturn(true);
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userId", userId);
            userMap.put("nickName", "nickname");
            userMap.put("account", "account");
            userTransUtilsMockedStatic.when(() -> UserTransUtils.getUserMap(user)).thenReturn(userMap);
            when(mockJwtProcessor.generateToken(userMap)).thenReturn(accessToken);
            when(mockJwtProcessor.generateRefreshToken(userMap)).thenReturn(newRefreshToken);

            String result = mockUserService.refreshAccessToken(refreshToken, userId);

            assertEquals(accessToken, result);

            verify(mockJwtProcessor).validateToken(refreshToken, userId);
            verify(mockUserMapper).selectByPrimaryKey(userId);
            verify(mockRedisProcessor).set(eq(RedisTransKey.refreshTokenKey(user.getEmail())), eq(newRefreshToken));
            verify(mockRedisProcessor).set(eq(RedisTransKey.tokenKey(user.getEmail())), eq(accessToken));
        }
    }
    @Test
    void refreshAccessToken_InvalidRefreshToken_ThrowsException() {
        String refreshToken = "invalidToken";
        Long userId = 1L;

        when(mockJwtProcessor.validateToken(refreshToken, userId)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                mockUserService.refreshAccessToken(refreshToken, userId)
        );

        assertEquals(ErrorCode.TOKEN_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("refreshToken验证失败,当前用户id：" + userId));

        verify(mockJwtProcessor).validateToken(refreshToken, userId);
        verify(mockUserMapper, never()).selectByPrimaryKey(anyLong());
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
    void selectUserByEmail() {
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
    void selectUserByEmail_Without_User() {
        when(mockUserMapper.findByEmail("2436056388@qq.com")).thenReturn(null);

        assertThatThrownBy(() -> mockUserService.selectUserByEmail("2436056388@qq.com")).isInstanceOf(BusinessException.class);
    }
    @Test
    void selectUsersByNickName_NickNameNotFound() {
        String nickName = "nonexistentNickName";
        int pageNo = 1;
        int pageSize = 10;

        when(mockUserMapper.selectUsersByNickName(nickName)).thenReturn(Collections.emptyList());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                mockUserService.selectUsersByNickName(nickName, pageNo, pageSize)
        );

        assertEquals("用户不存在 [\"未找到用户\"]", exception.getMessage());

        verify(mockUserMapper).selectUsersByNickName(nickName);
    }

    @Test
    void selectUsersByNickName() {
        String nickName = "existingNickName";
        int pageNo = 1;
        int pageSize = 10;
        User user = new User();
        user.setUserId(1L);
        user.setNickName(nickName);

        List<User> expectedUsers = Collections.singletonList(user);
        when(mockUserMapper.selectUsersByNickName(nickName)).thenReturn(expectedUsers);

        List<User> actualUsers = mockUserService.selectUsersByNickName(nickName, pageNo, pageSize);

        assertNotNull(actualUsers);
        assertEquals(expectedUsers, actualUsers);

        verify(mockUserMapper).selectUsersByNickName(nickName);
    }

    @Test
    public void getUsers() {
        int pageNo = 1;
        int pageSize = 2;
        User user1 = new User(1L, "Alice","","nickName1", "password1", "email1", "phone1", 1, "website1");
        User user2 = new User(2L, "Alice","","nickName2", "password1", "email2", "phone2", 1, "website1");

        List<User> expectedUsers = Arrays.asList(user1,user2);
        when(mockUserMapper.selectUsers()).thenReturn(expectedUsers);

        List<User> actualUsers = mockUserService.getUsers(pageNo, pageSize);

        assertNotNull(actualUsers);
        assertEquals(expectedUsers, actualUsers);
        verify(mockUserMapper).selectUsers();
    }

    @Test
    void selectUserByUserId() {
        Long userId = 1L;
        User expectedUser = new User();
        expectedUser.setUserId(userId);
        expectedUser.setAccount("account");
        expectedUser.setNickName("nickName");
        expectedUser.setPassword("password");
        expectedUser.setEmail("email");
        expectedUser.setPhone("phone");

        when(mockUserMapper.selectByPrimaryKey(userId)).thenReturn(expectedUser);

        User actualUser = mockUserService.selectUserByUserId(userId).get();

        assertEquals(expectedUser, actualUser);
    }

    @Test
    void deleteUserById() {
        Long userId = 1L;
        when(mockUserMapper.deleteByPrimaryKey(userId)).thenReturn(1);

        mockUserService.deleteUserById(userId);

        verify(mockUserMapper).deleteByPrimaryKey(userId);
    }



    @Test
    void updateUser() {
        User user = new User();
        user.setAccount("account");
        user.setNickName("nickName");
        user.setEmail("email");
        user.setPhone("phone");

        when(mockUserMapper.updateByPrimaryKeySelective(any(User.class))).thenReturn(1);

        mockUserService.updateUser(user);

        verify(mockUserMapper).updateByPrimaryKeySelective(any(User.class));
    }

    @Test
    void getTotalCount() {
        int expectedCount = 10;
        when(mockUserMapper.selectTotalCount()).thenReturn(expectedCount);
        int actualCount = mockUserService.getTotalCount();
        assertEquals(expectedCount, actualCount);
        verify(mockUserMapper).selectTotalCount();
    }
}
