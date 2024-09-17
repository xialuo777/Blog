package com.blog.mapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReflectUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class AssertHelperSipf {
    @Test
    void getRandomT1() {
        Poo1 poo1 = randomT(Poo1.class);
        assertThat(poo1).isNotNull();
        assertBean(poo1, poo1);
    }

    public static <T> T randomT(Class<T> clazz) {
        T bean = ReflectUtil.newInstanceIfPossible(clazz);
        Field[] fields = ReflectUtil.getFields(clazz);
        for (Field field : fields) {
            ReflectUtil.setFieldValue(bean, field, getRandomValue(field));
        }
        return bean;
    }

    public static <T> void assertBean(T b1, T b2) {
        Field[] fields = ReflectUtil.getFields(b1.getClass());
        for (Field field : fields) {
            Object fieldValue1 = ReflectUtil.getFieldValue(b1, field);
            Object fieldValue2 = ReflectUtil.getFieldValue(b2, field);
            assertField(fieldValue1, fieldValue2);
            assertField(fieldValue2, fieldValue1);
        }
    }

    public static void assertField(Object v1, Object v2) {
        if (ObjectUtil.isNull(v1) && ObjectUtil.isNull(v2)) {
            return;
        }
        assertThat(v1).isNotNull();
        assertThat(v2).isNotNull();

        Class<?> clazz = v1.getClass();
        if (clazz.isPrimitive())
            assertThat(v1.getClass()).isEqualTo(v2.getClass());

        if (long.class == clazz || Long.class == clazz) {
            assertThat((long) v1).isEqualTo(v2);
            return;
        } else if (int.class == clazz || Integer.class == clazz) {
            assertThat((int) v1).isEqualTo(v2);
            return;
        } else if (short.class == clazz) {
            assertThat((short) v1).isEqualTo(v2);
            return;
        } else if (char.class == clazz) {
            assertThat((char) v1).isEqualTo(v2);
            return;
        } else if (byte.class == clazz) {
            assertThat((byte) v1).isEqualTo(v2);
            return;
        } else if (double.class == clazz || Double.class == clazz) {
            assertThat((double) v1).isEqualTo(v2);
            return;
        } else if (float.class == clazz || Float.class == clazz) {
            assertThat((float) v1).isEqualTo(v2);
            return;
        } else if (boolean.class == clazz || Boolean.class == clazz) {
            assertThat((boolean) v1).isEqualTo(v2);
            return;
        } else if (BigDecimal.class == clazz) {
            double dd1 = ((BigDecimal) v1).doubleValue();
            double dd2 = ((BigDecimal) v2).doubleValue();
            assertThat(dd1).isEqualTo(dd2);
            return;
        } else if (String.class == clazz) {
            assertThat(v1.toString()).isEqualTo(v2.toString());
            return;
        } else if (Date.class == clazz || DateTime.class == clazz || java.sql.Date.class == clazz) {
            String date1 = DateUtil.format((Date) v1, DatePattern.NORM_DATETIME_MS_PATTERN);
            String date2 = DateUtil.format((Date) v2, DatePattern.NORM_DATETIME_MS_PATTERN);
            assertThat(date1).isEqualTo(date2);
            return;
        }else if (Byte.class == clazz){
            assertThat(v1.toString()).isEqualTo(v2.toString());
            return;
        }
        System.err.println("未识别的类型：" + clazz);
        fail();
    }

    private static Object getRandomValue(Field field) {
        Class<?> clazz = field.getType();
        if (long.class == clazz || Long.class == clazz) {
            return RandomUtil.randomLong(187654321L, 987654321L);
        } else if (int.class == clazz || Integer.class == clazz) {
            return RandomUtil.randomInt(100);
        } else if (short.class == clazz) {
            return (short) RandomUtil.randomInt(20);
        } else if (char.class == clazz) {
            return RandomUtil.randomChar();
        } else if (byte.class == clazz) {
            return (byte) 0;
        } else if (double.class == clazz || Double.class == clazz) {
            return RandomUtil.randomDouble(100D, 2, RoundingMode.CEILING);
        } else if (float.class == clazz || Float.class == clazz) {
            return 0f;
        } else if (boolean.class == clazz || Boolean.class == clazz) {
            return RandomUtil.randomBoolean();
        } else if (BigDecimal.class == clazz) {
            double dd = 0.0;
            do {
                dd = RandomUtil.randomDouble(7788, 2, RoundingMode.CEILING);
                return BigDecimal.valueOf(dd);
            } while (dd > 100);
        } else if (String.class == clazz) {
            return RandomUtil.randomString(1);
        } else if (Date.class == clazz) {
            return DateUtil.parse("2022-04", DatePattern.NORM_MONTH_PATTERN);
        }else if (Byte.class == clazz){
            return Byte.valueOf("0");
        }
        return null;
    }

    public static <T> T getDbRecord(Class<T> clazz, JdbcTemplate jdbcTemplate, String sql) {
        List<T> dbRecords = getDbRecords(clazz, jdbcTemplate, sql);
        assertThat(dbRecords.size()).isGreaterThan(0);
        return dbRecords.get(0);
    }

    public static <T> List<T> getDbRecords(Class<T> clazz, JdbcTemplate jdbcTemplate, String sql) {
        List<T> scms = Lists.newArrayList();
        List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql);
        for (Map<String, Object> map : maps) {
            T db_record = BeanUtil.fillBeanWithMap(map, ReflectUtil.newInstance(clazz), false);
            scms.add(db_record);
        }
        return scms;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    private static class Poo1 {
        private int i;
        private Integer i2;
        private double d;
        private Double d2;
        private long l;
        private Long l2;
        private BigDecimal amt;
        private String s;
        private boolean b;
        private Boolean b2;
    }
}
