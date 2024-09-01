package com.blog.util.bo;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Data
@Component
public class EmailCodeBo implements Serializable {
    private String code;
    private String email;


}
