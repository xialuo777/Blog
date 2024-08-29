package com.blog.entity;

import lombok.Data;

@Data
public class BlogTag {
    private Long id;
    private Long blogId;

    private Long tagId;
}
