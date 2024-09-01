package com.blog.util;

import java.util.*;
import java.math.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeUtiesTest {

    @InjectMocks
    private CodeUtils codeUtilsUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    /*测试生成code长度是否为6*/
    @Test
    void testGetCode_LengthIsSix() {
        String code = codeUtilsUnderTest.getCode();
        assertThat(code).hasSize(6);
    }

    /*测试仅由数字和大小写字母组成*/
    @Test
    void testGetCode_ContainsOnlyValidCharacters() {
        String code = codeUtilsUnderTest.getCode();
        assertTrue(code.chars().allMatch(ch ->
                (ch >= '0' && ch <= '9') ||
                        (ch >= 'a' && ch <= 'z') ||
                        (ch >= 'A' && ch <= 'Z')
        ));
    }
    /*测试是否由数字、大小写字母随机生成*/
    @Test
    void testGetCode_MixedCharacterTypes() {
        String code = codeUtilsUnderTest.getCode();
        boolean hasDigit = code.chars().anyMatch(ch -> ch >= '0' && ch <= '9');
        boolean hasLowercase = code.chars().anyMatch(ch -> ch >= 'a' && ch <= 'z');
        boolean hasUppercase = code.chars().anyMatch(ch -> ch >= 'A' && ch <= 'Z');
        assertTrue(hasDigit || hasLowercase || hasUppercase);
    }


}
