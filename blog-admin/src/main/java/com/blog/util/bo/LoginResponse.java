package com.blog.util.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
/**
 * @author: zhang
 * @time: 2024-09-14 12:54
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;

}