package com.blog.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@ApiModel(description = "blog")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Blog {
    @ApiModelProperty(value = "")
    private Long blogId;

    @ApiModelProperty(value = "")
    private String blogTitle;

    @ApiModelProperty(value = "")
    private Long userId;

    @ApiModelProperty(value = "")
    private String blogDesc;

    @ApiModelProperty(value = "")
    private String blogContent;

    @ApiModelProperty(value = "")
    private Long categoryId;

    @ApiModelProperty(value = "")
    private String categoryName;

    @ApiModelProperty(value = "")
    private Integer blogStatus;

    @ApiModelProperty(value = "")
    private String blogTags;

    @ApiModelProperty(value = "")
    private String thumbnail;

    @ApiModelProperty(value = "")
    private Long viewCount;

    @ApiModelProperty(value = "")
    private Date creatTime;

    @ApiModelProperty(value = "")
    private Date updateTime;

    @ApiModelProperty(value = "")
    private Integer isTop;

    @ApiModelProperty(value = "")
    private Integer enableComment;

    @ApiModelProperty(value = "")
    private Integer isDelete;

    @ApiModelProperty(value = "")
    private String subUrl;

}