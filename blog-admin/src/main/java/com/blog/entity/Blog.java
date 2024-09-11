package com.blog.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel(description = "blog")
@Data
public class Blog {
    @ApiModelProperty(value = "")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long blogId;

    @ApiModelProperty(value = "")
    private String blogTitle;

    @ApiModelProperty(value = "")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long userId;

    @ApiModelProperty(value = "")
    private String blogDesc;

    @ApiModelProperty(value = "")
    private String blogContent;

    @ApiModelProperty(value = "")
    private Integer categoryId;

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
    private Boolean enableComment;

    @ApiModelProperty(value = "")
    private Integer isDelete;

    @ApiModelProperty(value = "")
    private String subUrl;

}