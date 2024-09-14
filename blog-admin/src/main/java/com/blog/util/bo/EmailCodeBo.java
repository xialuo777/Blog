package com.blog.util.bo;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author: zhang
 * @time: 2024-09-14 12:53
 */
@Data
@Component
public class EmailCodeBo implements Serializable {
    private String code;
    private String email;


}
