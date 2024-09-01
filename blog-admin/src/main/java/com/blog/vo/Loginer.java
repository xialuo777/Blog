package com.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

/**
 * @ClassName Loginer
 * @Description: 登录用户实体类
 * @Author: 张
 * @Time 2024-08-22 16:21
 * 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Loginer {
    @Email(message = "请正确输入注册邮箱")
    private String email;
    @NotNull(message = "用户密码不能为空")
    @Length(min = 6,max = 18,message = "用户密码长度在6-18位")
    private String password;
}
