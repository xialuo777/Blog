package com.blog.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class SecurityUtilsTest {

    @InjectMocks
    private SecurityUtils securityUtils;

    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
    }
    /*测试密码是否加密*/
    @Test
    public void encodePassword_PasswordIsEncoded_EncodedPasswordMatchesExpected() {
        String password = "password";
        String encodedPassword = SecurityUtils.encodePassword(password);
        assertTrue(passwordEncoder.matches(password, encodedPassword));
    }


    /*测试相同密码加密后的结果仍然相同*/
    @Test
    public void checkPassword_ValidPassword_ReturnsTrue() {
        String password = "password";
        String encodedPassword = passwordEncoder.encode(password);

        assertTrue(securityUtils.checkPassword(password, encodedPassword));
    }
    /*测试不同密码加密后的结果仍然不同*/
    @Test
    public void checkPassword_InvalidPassword_ReturnsFalse() {
        String password = "password";
        String encodedPassword = passwordEncoder.encode("wrongPassword");

        assertFalse(securityUtils.checkPassword(password, encodedPassword));
    }

}
