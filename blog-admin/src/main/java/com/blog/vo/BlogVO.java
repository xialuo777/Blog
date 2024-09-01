package com.blog.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;

@Data
@ApiModel(description = "博客Request")
public class BlogVO {
    /*博客id*/
    @ApiModelProperty(value = "博客id")
    private Long blogId;

    /*博客标题*/
    @NotBlank(message = "博客标题不能为空")
    private String blogTitle;
    /*博客摘要*/
    @NotBlank(message = "博客概要不能为空")
    private String articleDesc;
    @NotBlank(message = "博客内容不能为空")
    private String blogContent;
    /*博客类型*/
    private Integer blogType;
    /*博客分类名*/
    @NotBlank(message = "文章分类不能为空")
    @ApiModelProperty(value = "分类名", required = true)
    private String categoryName;
    /*博客标签名*/
    private List<String> tagNameList;
    /*博客当前状态*/
    private Integer blogStatus;




}
