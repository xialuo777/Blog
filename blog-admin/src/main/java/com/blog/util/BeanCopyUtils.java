package com.blog.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 拷贝工具
 */
@Slf4j
public class BeanCopyUtils {

    public static void copyBeanSelective(Object source, Object target) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue != null) {
                BeanWrapper targetWrapper = new BeanWrapperImpl(target);
                Object targetValue = targetWrapper.getPropertyValue(pd.getName());
                if (targetValue == null) {
                    targetWrapper.setPropertyValue(pd.getName(), srcValue);
                }
            }
        }
    }

}
