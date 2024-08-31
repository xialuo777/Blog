package com.blog.util.bo;

import com.blog.util.CodeUties;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Data
@Component
public class EmailCodeBo implements Serializable {
    private String code;
    private String email;


}
