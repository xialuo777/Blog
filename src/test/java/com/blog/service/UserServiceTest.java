package com.blog.service;

import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.mapper.UserMapper;
import com.blog.util.SecurityUtils;
import com.blog.util.SnowFlakeUtil;
import com.blog.vo.Loginer;
import com.blog.vo.Register;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@RunWith(SpringRunner.class)
class UserServiceTest {

    @Mock
    private UserMapper mockUserMapper;

    @InjectMocks
    private UserService userServiceUnderTest;

    private HttpSessionBO sessionBO;
    private Loginer loginer;

    @BeforeEach
    void setUp() {
        loginer = new Loginer("email","password");
        sessionBO = new HttpSessionBO("2436056388@qq.com", "passcd");
    }

/*正常登录*/
    @Test
    void testUserLogin() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class);
        ){
            final User user = new User();
            user.setUserId(0L);
            user.setAccount("account");
            user.setNickName("nickName");
            user.setPassword(SecurityUtils.encodePassword("password"));
            user.setEmail("email");
            when(mockUserMapper.findByEmail("email")).thenReturn(user);
            securityUtilsMockedStatic.when(()->SecurityUtils.checkPassword("password",user.getPassword())).thenReturn(true);

            userServiceUnderTest.userLogin(loginer);
            verify(mockUserMapper, times(1)).findByEmail("email");
        }

    }
/*用户不存在*/
    @Test
    void testUserLogin_withWrongEmail() {
        final User user = new User();
        user.setUserId(0L);
        user.setAccount("account");
        user.setNickName("nickName");
        user.setPassword(SecurityUtils.encodePassword("password"));
        user.setEmail("email1");
        when(mockUserMapper.findByEmail("email")).thenReturn(null);

        assertThrows(BusinessException.class,()->userServiceUnderTest.userLogin(loginer));
    }
/*密码不对*/
    @Test
    void testUserLogin_withWrongPassword() {

        final User user = new User();
        user.setUserId(0L);
        user.setAccount("account");
        user.setNickName("nickName");
        user.setPassword(SecurityUtils.encodePassword("passwords"));
        user.setEmail("email");
        when(mockUserMapper.findByEmail("email")).thenReturn(user);

        assertThrows(BusinessException.class,()->userServiceUnderTest.userLogin(loginer));

    }



    @Test
    void testUserRegister_Success() {
        try (MockedStatic<SnowFlakeUtil> snowFlakeUtilMockedStatic = mockStatic(SnowFlakeUtil.class); MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)){
            Register register = new Register();
            register.setAccount("123456");
            register.setNickName("juniTest");
            register.setPassword("123456789");
            register.setCheckPassword("123456789");
            register.setEmail("2436056388@qq.com");
            register.setPhone("18539246184");
            register.setEmailCode("passcd");

            snowFlakeUtilMockedStatic.when(SnowFlakeUtil::nextId).thenReturn(1L);
            securityUtilsMockedStatic.when(() -> SecurityUtils.encodePassword("123456789")).thenReturn("encodedPassword");

            User user = new User();
            user.setUserId(1L);
            user.setAccount("123456");
            user.setNickName("juniTest");
            user.setPassword("encodedPassword");
            user.setEmail("2436056388@qq.com");
            user.setPhone("18539246184");

            doNothing().when(mockUserMapper).insertUser(any(User.class));

            userServiceUnderTest.userRegister(register, sessionBO);

            verify(mockUserMapper, times(1)).insertUser(any(User.class));

        }



    }
    /*两次输入密码不一致*/
    @Test
    void testUserRegister_DifferentPasswords() {

        Register register = new Register();
        register.setAccount("123456");
        register.setNickName("juniTest");
        register.setPassword("123456789");
        register.setCheckPassword("12345678");
        register.setEmail("2436056388@qq.com");
        register.setPhone("18539246184");
        register.setEmailCode("passcd");

        assertThrows(BusinessException.class, () -> userServiceUnderTest.userRegister(register, sessionBO));
    }
    /*邮箱验证码输入有误*/
    @Test
    void testUserRegister_InvalidEmailCode() {
        Register register = new Register();
        register.setAccount("123456");
        register.setNickName("juniTest");
        register.setPassword("123456789");
        register.setCheckPassword("123456789");
        register.setEmail("2436056388@qq.com");
        register.setPhone("18539246184");
        register.setEmailCode("pass");

        assertThrows(BusinessException.class, () -> userServiceUnderTest.userRegister(register, sessionBO));
    }
    /*邮箱输入有误*/
    @Test
    void testUserRegister_withWrongEmail() {
        Register register = new Register();
        register.setAccount("123456");
        register.setNickName("juniTest");
        register.setPassword("123456789");
        register.setCheckPassword("123456789");
        register.setEmail("2436056388");
        register.setPhone("18539246184");
        register.setEmailCode("passcd");

        assertThrows(BusinessException.class, () -> userServiceUnderTest.userRegister(register, sessionBO));
    }



    @Test
    void testSelectUserByEmail() {
        final User expectedResult = new User();
        expectedResult.setUserId(0L);
        expectedResult.setAccount("account");
        expectedResult.setNickName("nickName");
        expectedResult.setPassword("password");
        expectedResult.setEmail("email");
        expectedResult.setPhone("phone");
        when(mockUserMapper.findByEmail("email")).thenReturn(expectedResult);
        final User user = new User();
        user.setUserId(0L);
        user.setAccount("account");
        user.setNickName("nickName");
        user.setPassword("password");
        user.setEmail("email");
        user.setPhone("phone");
        final User result = userServiceUnderTest.selectUserByEmail("email");

        assertThat(result).isEqualTo(expectedResult);
    }
    /*输入邮箱并未注册账号*/
    @Test
    void testSelectUserByEmail_withoutUser() {
        when(mockUserMapper.findByEmail("email")).thenReturn(null);

        assertThrows(BusinessException.class, ()->userServiceUnderTest.selectUserByEmail("email"));
    }

    /*输入邮箱为空*/
    @Test
    void testSelectUserByEmail_withEmptyEmail() {
        final User expectedResult = new User();
        expectedResult.setUserId(0L);
        expectedResult.setAccount("account");
        expectedResult.setNickName("nickName");
        expectedResult.setPassword("password");
        expectedResult.setEmail("email");
        expectedResult.setPhone("phone");

        assertThrows(BusinessException.class, ()->userServiceUnderTest.selectUserByEmail(""));
    }
}
