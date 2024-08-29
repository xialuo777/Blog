package com.blog.mapper;

import com.blog.entity.Blog;
import com.blog.vo.BlogConditionQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface BlogMapper {
    int deleteById(Long blogId);
    int insertBlog(Blog blog);
    Blog selectById(Long blogId);
    int updateById(Blog blog);

    /**
     * 根据类别或者标签查询文章
     * @param blogConditionQuery
     * @return 文章列表
     */
    List<BlogConditionQuery> selectBlogListByCondition( BlogConditionQuery blogConditionQuery);
}
