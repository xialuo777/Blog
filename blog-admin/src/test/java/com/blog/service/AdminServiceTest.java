package com.blog.service;

import com.blog.entity.Admin;
import com.blog.exception.BusinessException;
import com.blog.mapper.AdminMapper;
import com.blog.util.JwtProcessor;
import com.blog.util.bo.LoginResponse;
import com.blog.util.redis.RedisProcessor;
import com.blog.vo.admin.AdminInVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminMapper mockAdminMapper;
    @Mock
    private JwtProcessor mockJwtProcessor;
    @Mock
    private RedisProcessor mockRedisProcessor;

    private AdminService adminServiceUnderTest;

    @BeforeEach
    void setUp() {
        adminServiceUnderTest = new AdminService(mockAdminMapper, mockJwtProcessor, mockRedisProcessor);
    }

    @Test
    void adminLogin() {
        final AdminInVo adminInVo = new AdminInVo("account", "password");
        final LoginResponse expectedResult = new LoginResponse("accessToken", "refreshToken");
        when(mockAdminMapper.selectByAccount("account")).thenReturn(new Admin(0L, "account", "password"));
        when(mockJwtProcessor.generateToken(anyObject())).thenReturn("accessToken");
        when(mockJwtProcessor.generateRefreshToken(anyObject())).thenReturn("refreshToken");

        final LoginResponse result = adminServiceUnderTest.adminLogin(adminInVo);

        assertThat(result).isEqualTo(expectedResult);
        verify(mockRedisProcessor).set("user:token:account", "accessToken", 7L, TimeUnit.DAYS);
    }
    @Test
    void adminLogin_WithWrongPassword() {
        final AdminInVo adminInVo = new AdminInVo("account", "password1");
        when(mockAdminMapper.selectByAccount("account")).thenReturn(new Admin(0L, "account", "password"));
        assertThrows(BusinessException.class, () -> adminServiceUnderTest.adminLogin(adminInVo));
    }


    @Test
    void getAdminById() {

        final Optional<Admin> expectedResult = Optional.of(new Admin(0L, "account", "password"));
        when(mockAdminMapper.selectByPrimaryKey(0L)).thenReturn(new Admin(0L, "account", "password"));

        final Optional<Admin> result = adminServiceUnderTest.getAdminById(0L);

        assertThat(result).isEqualTo(expectedResult);
    }


    @Test
    void updateAdmin() {

        final Admin admin = new Admin(0L, "account", "password");

        adminServiceUnderTest.updateAdmin(admin);

        verify(mockAdminMapper).updateByPrimaryKeySelective(new Admin(0L, "account", "password"));
    }
}
