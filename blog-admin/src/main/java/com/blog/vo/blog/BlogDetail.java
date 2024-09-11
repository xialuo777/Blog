package com.blog.vo.blog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogDetail extends BlogDesc{
    private String blogDesc;
    private String blogContent;
    private String blogTags;
    private String thumbnail;
    private Integer isTop;
    private Integer blogStatus;

}
