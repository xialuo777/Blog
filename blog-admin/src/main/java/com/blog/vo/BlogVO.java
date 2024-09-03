package com.blog.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "文章VO")
public class BlogVO {

    /**
     * 文章缩略图
     */
    @ApiModelProperty(value = "文章缩略图")
    private String thumbnail;

    /**
     * 文章标题
     */
    @NotBlank(message = "文章标题不能为空")
    @ApiModelProperty(value = "文章标题", required = true)
    private String blogTitle;

    /**
     * 文章概要
     */
    @NotBlank(message = "文章概要不能为空")
    @ApiModelProperty(value = "文章概要", required = true)
    private String blogDesc;

    /**
     * 文章内容
     */
    @NotBlank(message = "文章内容不能为空")
    @ApiModelProperty(value = "文章内容", required = true)
    private String blogContent;

    /**
     * 分类名
     */
    @NotBlank(message = "文章分类不能为空")
    @ApiModelProperty(value = "分类名", required = true)
    private Integer categoryId;

    /**
     * 标签名
     */
    @ApiModelProperty(value = "标签名")
    private String tagNames;

    /**
     * 是否置顶 (0否 1是)
     */
    @ApiModelProperty(value = "是否置顶 (0否 1是)", required = true)
    private Integer topFlag;


    /**
     * 状态 (1公开 2私密 3草稿)
     */
    @ApiModelProperty(value = "状态 (1公开 2私密 3草稿)", required = true)
    private Integer blogStatus;

}
