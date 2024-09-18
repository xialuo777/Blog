package com.blog.authentication;

import java.util.*;
import java.math.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

class CurrentUserHolderTest {

    @InjectMocks
    private CurrentUserHolder currentUserHolderUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void setUserId_ShouldSetTheUserId() {
        Long userId = 1L;
        currentUserHolderUnderTest.setUserId(userId);

        assertThat(currentUserHolderUnderTest.getUserId()).isEqualTo(userId);
    }

    @Test
    void getUserId_ShouldReturnTheSetUserId() {
        Long userId = 1L;
        currentUserHolderUnderTest.setUserId(userId);

        final Long result = currentUserHolderUnderTest.getUserId();

        assertThat(result).isEqualTo(userId);
    }

    @Test
    void clear_ShouldClearTheUserId() {
        Long userId = 1L;
        currentUserHolderUnderTest.setUserId(userId);
        currentUserHolderUnderTest.clear();

        assertThat(currentUserHolderUnderTest.getUserId()).isNull();
    }
}
