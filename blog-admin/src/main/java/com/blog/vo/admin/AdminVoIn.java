package com.blog.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 *  管理员登录对象
 *
 * @author : [24360]
 * @version : [v1.0]
 * @time 2024-09-13 17:28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminVoIn {
    @NotBlank
    private String account;
    @NotBlank
    private String password;
}
