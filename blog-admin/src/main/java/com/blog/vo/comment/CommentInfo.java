package com.blog.vo.comment;


import lombok.Data;

import java.util.Date;

@Data
public class CommentInfo {
    private String commentator;
    private String commentBody;
    private Date commentCreateTime;
}
