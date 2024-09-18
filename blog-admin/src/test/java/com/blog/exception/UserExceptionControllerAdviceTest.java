package com.blog.exception;

import java.util.*;
import java.math.*;

import com.blog.enums.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserExceptionControllerAdviceTest {

    @InjectMocks
    private UserExceptionControllerAdvice userExceptionControllerAdviceUnderTest;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void handleMethodArgumentNotValidException_WithFieldErrors_ReturnsErrorResponse() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        FieldError fieldError = new FieldError("objectName", "field", "error");
        when(bindingResult.getAllErrors()).thenReturn(java.util.Collections.singletonList(fieldError));

        ResponseResult<Map<String, String>> result = userExceptionControllerAdviceUnderTest.handleMethodArgumentNotValidException(ex);

        Map<String, String> expectedErrors = new HashMap<>();
        expectedErrors.put("field", "error");
        assertEquals(ResponseResult.fail(expectedErrors.toString()), result);
    }

    @Test
    void handleMethodArgumentNotValidException_NoFieldErrors_ReturnsEmptyErrorResponse() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(java.util.Collections.emptyList());

        ResponseResult<Map<String, String>> result = userExceptionControllerAdviceUnderTest.handleMethodArgumentNotValidException(ex);

        Map<String, String> expectedErrors = new HashMap<>();
        assertEquals(ResponseResult.fail(expectedErrors.toString()), result);
    }

    @Test
    void handleBusinessException_WithCodeAndMessage_ReturnsExpectedResponse() {
        BusinessException ex = new BusinessException(ErrorCode.PARAMS_ERROR.getCode(), "参数异常");


        ResponseResult<Object> expectedResponse = ResponseResult.fail(ErrorCode.PARAMS_ERROR.getCode(), "参数异常");

        ResponseResult<Object> result = userExceptionControllerAdviceUnderTest.handleBusinessException(ex);

        assertEquals(expectedResponse, result);

    }


}
