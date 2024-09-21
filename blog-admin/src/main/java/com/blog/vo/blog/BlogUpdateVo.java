package com.blog.vo.blog;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@ApiModel(description = "修改文章Vo")
@AllArgsConstructor
@NoArgsConstructor
public class BlogUpdateVo {
    /**
     * 文章id
     */
    @JsonProperty("blogId")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long blogId;


    /**
     * 文章缩略图
     */
    @ApiModelProperty(value = "文章缩略图")
    private String thumbnail;

    /**
     * 文章标题
     */
    @ApiModelProperty(value = "文章标题", required = true)
    private String blogTitle;

    /**
     * 文章概要
     */
    @ApiModelProperty(value = "文章概要", required = true)
    private String blogDesc;

    /**
     * 文章内容
     */
    @ApiModelProperty(value = "文章内容", required = true)
    private String blogContent;

    /**
     * 分类名
     */
    @ApiModelProperty(value = "分类Id", required = true)
    private Integer categoryId;

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
