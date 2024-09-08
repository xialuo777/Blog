package com.blog.vo.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVo {
    private Long userId;
    private String nickName;
    private String email;
    private String website;
}
