package com.blog.util.bo;

import lombok.Data;

/**
 * [修改密码]
 *
 * @author : [24360]
 * @version : [v1.0]
 * @createTime : [2024/9/21 11:19]
 */
@Data
public class PasswordBo {
    private String oldPassword;
    private String newPassword;
}
