package com.blog.vo.blog;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
@Data
public class BlogDesc {
    private String blogTitle;
    private Date creatTime;
    private Date updateTime;
}
