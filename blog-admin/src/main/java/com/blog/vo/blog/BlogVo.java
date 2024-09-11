package com.blog.vo.blog;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "文章VO")
public class BlogVo {

    /**
     * 文章id
     */
    @JsonProperty("blogId")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long blogId;
    /**
     * 用户id
     */
    @JsonProperty("userId")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;
    /**
     * 文章路径
     */
    private String subUrl;

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
     * 分类id
     */
    @NotBlank(message = "文章分类id不能为空")
    @ApiModelProperty(value = "分类id", required = true)
    private Integer categoryId;
    /**
     * 分类名
     */
    @NotBlank(message = "文章分类名不能为空")
    @ApiModelProperty(value = "分类名", required = true)
    private String categoryName;

    /**
     * 标签名
     */
    @ApiModelProperty(value = "标签名")
    private String blogTags;

    /**
     * 是否置顶 (0否 1是)
     */
    @ApiModelProperty(value = "是否置顶 (0否 1是)", required = true)
    private Integer isTop;


    /**
     * 状态 (1公开 2私密 3草稿)
     */
    @ApiModelProperty(value = "状态 (1公开 2私密 3草稿)", required = true)
    private Integer blogStatus;

}
