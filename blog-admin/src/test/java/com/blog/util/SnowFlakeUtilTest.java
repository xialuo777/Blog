package com.blog.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class SnowFlakeUtilTest {
    /*测试生成id的唯一性*/
    @Test
    public void testUniqueness() {
        long id1 = SnowFlakeUtil.nextId();
        long id2 = SnowFlakeUtil.nextId();
        assertTrue(id1 != id2);
    }
    /*测试生成id的连续性*/
    @Test
    public void testIdContinuity() {
        long[] ids = new long[100];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = SnowFlakeUtil.nextId();
            System.out.println(ids[i]);
        }
        for (int i = 1; i < ids.length; i++) {
            assertTrue(ids[i] - ids[i - 1] >= 1);
        }
    }


}




