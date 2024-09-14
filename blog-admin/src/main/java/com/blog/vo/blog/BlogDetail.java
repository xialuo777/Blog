package com.blog.vo.blog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author: zhang
 * @time: 2024-09-14 14:10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class BlogDetail extends BlogDesc{
    private String blogDesc;
    private Long userId;
    private String blogContent;
    private String blogTags;
    private String thumbnail;
    private Integer isTop;
    private Integer blogStatus;

}
