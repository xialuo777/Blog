package com.blog.vo.comment;


import lombok.Data;

import java.util.Date;

/**
 * @author: zhang
 * @time: 2024-09-14 14:10
 */
@Data
public class CommentOutVo {
    private String commentator;
    private String commentBody;
    private Date commentCreateTime;
}
