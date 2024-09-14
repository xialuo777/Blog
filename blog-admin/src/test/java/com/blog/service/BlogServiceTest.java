 package com.blog.service;

import com.blog.authentication.CurrentUserHolder;
import com.blog.constant.Constant;
import com.blog.entity.Blog;
import com.blog.entity.BlogTag;
import com.blog.entity.Category;
import com.blog.entity.Tag;
import com.blog.exception.BusinessException;
import com.blog.mapper.BlogMapper;
import com.blog.mapper.BlogTagMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.TagMapper;
import com.blog.util.SnowFlakeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogServiceTest {

    @Mock
    private CategoryMapper mockCategoryMapper;
    @Mock
    private BlogMapper mockBlogMapper;
    @Mock
    private TagMapper mockTagMapper;
    @Mock
    private BlogTagMapper mockBlogTagMapper;
    @Mock
    private CurrentUserHolder mockCurrentUserHolder;
    @InjectMocks
    private BlogService blogServiceUnderTest;

    @BeforeEach
    void setUp() {
        blogServiceUnderTest = new BlogService(mockCategoryMapper, mockBlogMapper, mockTagMapper, mockBlogTagMapper,
                mockCurrentUserHolder);
    }

    @Test
    void testSaveBlog() {
        try(MockedStatic<SnowFlakeUtil> snowFlakeUtilMocked = Mockito.mockStatic(SnowFlakeUtil.class)) {
            final Blog blog = new Blog();
            blog.setCategoryId(1L);
            blog.setCategoryName("categoryName");
            blog.setBlogTags("blogTags");

            Category categoryExist = new Category(1L, "categoryName");
            when(mockCategoryMapper.selectByPrimaryKey(1L)).thenReturn(categoryExist);
            when(mockCurrentUserHolder.getUserId()).thenReturn(2L);
            snowFlakeUtilMocked.when(()->SnowFlakeUtil.nextId()).thenReturn(1L);

            final List<Tag> tags = Arrays.asList(
                    new Tag(0L, "blogTags", new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime(), 0));
            when(mockTagMapper.selectListByTagNames(Arrays.asList("blogTags"))).thenReturn(tags);
            blogServiceUnderTest.saveBlog(blog);

            final Blog record = new Blog();
            record.setBlogId(1L);
            record.setUserId(2L);
            record.setCategoryId(1L);
            record.setCategoryName("categoryName");
            record.setBlogTags("blogTags");
            String baseHomePageUrl = String.format(Constant.BLOG_BASE_PATH + "%s/%s", blog.getUserId(), blog.getBlogId());
            record.setSubUrl(baseHomePageUrl);
            verify(mockBlogMapper).insertSelective(record);
            verify(mockCategoryMapper).increatCategoryRank(new Category(1L, "categoryName"));
            verify(mockBlogTagMapper).insertList(Arrays.asList(new BlogTag(1L, 0L)));
        }
    }
    @Test
    void testSaveBlog_NewCategory() {
        try(MockedStatic<SnowFlakeUtil> snowFlakeUtilMocked = Mockito.mockStatic(SnowFlakeUtil.class)) {
            final Blog blog = new Blog();
            blog.setCategoryId(1L);
            blog.setCategoryName("categoryName");
            blog.setBlogTags("blogTags");

            when(mockCategoryMapper.selectByPrimaryKey(1L)).thenReturn(null);
            when(mockCurrentUserHolder.getUserId()).thenReturn(2L);
            snowFlakeUtilMocked.when(()->SnowFlakeUtil.nextId()).thenReturn(1L);

            final List<Tag> tags = Arrays.asList(
                    new Tag(0L, "blogTags", new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime(), 0));
            when(mockTagMapper.selectListByTagNames(Arrays.asList("blogTags"))).thenReturn(tags);

            blogServiceUnderTest.saveBlog(blog);

            final Blog record = new Blog();
            record.setBlogId(1L);
            record.setUserId(2L);
            record.setCategoryId(1);
            record.setCategoryName("categoryName");
            record.setBlogTags("blogTags");
            String baseHomePageUrl = String.format(Constant.BLOG_BASE_PATH + "%s/%s", blog.getUserId(), blog.getBlogId());
            record.setSubUrl(baseHomePageUrl);
            verify(mockBlogMapper).insertSelective(record);
            verify(mockCategoryMapper).insertSelective(new Category(1L, "categoryName"));
            verify(mockBlogTagMapper).insertList(Arrays.asList(new BlogTag(1L, 0L)));
        }
    }

    @Test
    void testSaveBlog_NewTag() {
        try (MockedStatic<SnowFlakeUtil> snowFlakeUtilMocked = Mockito.mockStatic(SnowFlakeUtil.class)) {
            final Blog blog = new Blog();
            blog.setCategoryId(1L);
            blog.setCategoryName("categoryName");
            blog.setBlogTags("newTag");

            Category categoryExist = new Category(1L, "categoryName");
            when(mockCategoryMapper.selectByPrimaryKey(1L)).thenReturn(categoryExist);
            when(mockCurrentUserHolder.getUserId()).thenReturn(2L);
            snowFlakeUtilMocked.when(() -> SnowFlakeUtil.nextId()).thenReturn(1L);

            when(mockTagMapper.selectListByTagNames(Arrays.asList("newTag"))).thenReturn(Collections.emptyList());

            blogServiceUnderTest.saveBlog(blog);

            verify(mockTagMapper).insertList(Arrays.asList(new Tag("newTag")) );
            verify(mockBlogTagMapper).insertList(Arrays.asList(new BlogTag(1L, null)));
        }
    }
    @Test
    void testSaveBlog_TooManyTags() {
       try(MockedStatic<SnowFlakeUtil> snowFlakeUtilMocked = Mockito.mockStatic(SnowFlakeUtil.class)) {
           final Blog blog = new Blog();
           blog.setCategoryId(1L);
           blog.setCategoryName("categoryName");
           blog.setBlogTags("tag1,tag2,tag3,tag4,tag5,tag6,tag7");

           when(mockCategoryMapper.selectByPrimaryKey(1L)).thenReturn(new Category(1, "categoryName"));
           when(mockCurrentUserHolder.getUserId()).thenReturn(2L);
           snowFlakeUtilMocked.when(()->SnowFlakeUtil.nextId()).thenReturn(1L);

           assertThatThrownBy(() -> blogServiceUnderTest.saveBlog(blog))
                   .isInstanceOf(BusinessException.class);
       }
    }
    @Test
    void testUpdateBlog_NewCategory() {
        final Blog blog = new Blog();
        blog.setBlogId(0L);
        blog.setUserId(0L);
        blog.setCategoryId(1L);
        blog.setCategoryName("newCategoryName");
        blog.setBlogTags("blogTags");
        blog.setSubUrl("subUrl");

        final Blog blogDb = new Blog();
        blogDb.setBlogId(0L);
        blogDb.setUserId(0L);
        blogDb.setCategoryId(0L);
        blogDb.setCategoryName("categoryName");
        blogDb.setBlogTags("blogTags");
        blogDb.setSubUrl("subUrl");
        when(mockBlogMapper.selectByPrimaryKey(0L)).thenReturn(blogDb);

        Category oldCategory  = null;
        when(mockCategoryMapper.selectByPrimaryKey(anyLong())).thenReturn(oldCategory);

        final List<Tag> tags = Arrays.asList(
                new Tag(0L, "blogTags", new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime(), 0));
        when(mockTagMapper.selectListByTagNames(Arrays.asList("blogTags"))).thenReturn(tags);

        blogServiceUnderTest.updateBlog(blog);

        final Blog record = new Blog();
        record.setBlogId(0L);
        record.setUserId(0L);
        record.setCategoryId(1L);
        record.setCategoryName("newCategoryName");
        record.setBlogTags("blogTags");
        record.setSubUrl("subUrl");
        verify(mockBlogMapper).updateByPrimaryKeySelective(record);
        verify(mockCategoryMapper).insertSelective(new Category(1L,"newCategoryName"));
        verify(mockBlogTagMapper).insertList(Arrays.asList(new BlogTag(0L, 0L)));
    }

    @Test
    void testUpdateBlog_CategoryExists() {
        final Blog blog = new Blog();
        blog.setBlogId(1L);
        blog.setUserId(2L);
        blog.setCategoryId(1L);
        blog.setCategoryName("newCategoryName");
        blog.setBlogTags("blogTags");
        blog.setSubUrl("subUrl");

        final Blog blogDb = new Blog();
        blogDb.setBlogId(1L);
        blogDb.setUserId(2L);
        blogDb.setCategoryId(1L);
        blogDb.setCategoryName("categoryName");
        blogDb.setBlogTags("blogTags");
        blogDb.setSubUrl("subUrl");
        when(mockBlogMapper.selectByPrimaryKey(1L)).thenReturn(blogDb);

        Category categoryExist = new Category(1L, "categoryName");
        when(mockCategoryMapper.selectByPrimaryKey(1L)).thenReturn(categoryExist);

        final List<Tag> tags = Arrays.asList(
                new Tag(0L, "blogTags", new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime(), 0));
        when(mockTagMapper.selectListByTagNames(Arrays.asList("blogTags"))).thenReturn(tags);

        blogServiceUnderTest.updateBlog(blog);

        final Blog record = new Blog();
        record.setBlogId(1L);
        record.setUserId(2L);
        record.setCategoryId(1L);
        record.setCategoryName("newCategoryName");
        record.setBlogTags("blogTags");
        record.setSubUrl("subUrl");
        verify(mockBlogMapper).updateByPrimaryKeySelective(record);
        verify(mockCategoryMapper).increatCategoryRank(categoryExist);
        verify(mockBlogTagMapper).insertList(Arrays.asList(new BlogTag(1L, 0L)));
    }
    @Test
    void testUpdateBlog_NewTag() {
        final Blog blog = new Blog();
        blog.setBlogId(1L);
        blog.setUserId(2L);
        blog.setCategoryId(1L);
        blog.setCategoryName("categoryName");
        blog.setBlogTags("newTag");
        blog.setSubUrl("subUrl");

        final Blog blogDb = new Blog();
        blogDb.setBlogId(1L);
        blogDb.setUserId(2L);
        blogDb.setCategoryId(1L);
        blogDb.setCategoryName("categoryName");
        blogDb.setBlogTags("blogTags");
        blogDb.setSubUrl("subUrl");
        when(mockBlogMapper.selectByPrimaryKey(1L)).thenReturn(blogDb);

        Category categoryExist = new Category(1L, "categoryName");
        when(mockCategoryMapper.selectByPrimaryKey(1L)).thenReturn(categoryExist);

        when(mockTagMapper.selectListByTagNames(Arrays.asList("newTag"))).thenReturn(Collections.emptyList());

        blogServiceUnderTest.updateBlog(blog);

        verify(mockTagMapper).insertList(Arrays.asList(new Tag("newTag")));
        verify(mockBlogTagMapper).insertList(Arrays.asList(new BlogTag(1L, null)));
    }
    @Test
    void testUpdateBlog_TooManyTags() {
        final Blog blog = new Blog();
        blog.setBlogId(1L);
        blog.setUserId(2L);
        blog.setCategoryId(1L);
        blog.setCategoryName("categoryName");
        blog.setBlogTags("tag1,tag2,tag3,tag4,tag5,tag6,tag7");
        blog.setSubUrl("subUrl");

        final Blog blogDb = new Blog();
        blogDb.setBlogId(1L);
        blogDb.setUserId(2L);
        blogDb.setCategoryId(1L);
        blogDb.setCategoryName("categoryName");
        blogDb.setBlogTags("blogTags");
        blogDb.setSubUrl("subUrl");
        when(mockBlogMapper.selectByPrimaryKey(1L)).thenReturn(blogDb);

        Category categoryExist = new Category(1L, "categoryName");
        when(mockCategoryMapper.selectByPrimaryKey(1L)).thenReturn(categoryExist);

        assertThatThrownBy(() -> blogServiceUnderTest.updateBlog(blog))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void testGetBlogList() {
        final Blog blog = new Blog();
        blog.setBlogId(0L);
        blog.setUserId(0L);
        blog.setCategoryId(0L);
        blog.setCategoryName("categoryName");
        blog.setBlogTags("blogTags");
        blog.setSubUrl("subUrl");
        final List<Blog> expectedResult = Arrays.asList(blog);

        final Blog blog1 = new Blog();
        blog1.setBlogId(0L);
        blog1.setUserId(0L);
        blog1.setCategoryId(0L);
        blog1.setCategoryName("categoryName");
        blog1.setBlogTags("blogTags");
        blog1.setSubUrl("subUrl");
        final List<Blog> blogs = Arrays.asList(blog1);
        when(mockBlogMapper.selectListByUserId(0L)).thenReturn(blogs);

        final List<Blog> result = blogServiceUnderTest.getBlogListByUserId(0L, 0, 0);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetBlogList_BlogMapperReturnsNoItems() {
        when(mockBlogMapper.selectListByUserId(0L)).thenReturn(Collections.emptyList());

        final List<Blog> result = blogServiceUnderTest.getBlogListByUserId(0L, 0, 0);

        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testGetBlogById() {

        final Blog expectedResult = new Blog();
        expectedResult.setBlogId(0L);
        expectedResult.setUserId(0L);
        expectedResult.setCategoryId(0L);
        expectedResult.setCategoryName("categoryName");
        expectedResult.setBlogTags("blogTags");
        expectedResult.setSubUrl("subUrl");

        final Blog blog = new Blog();
        blog.setBlogId(0L);
        blog.setUserId(0L);
        blog.setCategoryId(0L);
        blog.setCategoryName("categoryName");
        blog.setBlogTags("blogTags");
        blog.setSubUrl("subUrl");
        when(mockBlogMapper.selectByPrimaryKey(0L)).thenReturn(blog);

        final Blog result = blogServiceUnderTest.getBlogById(0L).get();


        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetBlogListByCategoryId() {

        final Blog blog = new Blog();
        blog.setBlogId(0L);
        blog.setUserId(0L);
        blog.setCategoryId(0L);
        blog.setCategoryName("categoryName");
        blog.setBlogTags("blogTags");
        blog.setSubUrl("subUrl");
        final List<Blog> expectedResult = Arrays.asList(blog);

        final Blog blog1 = new Blog();
        blog1.setBlogId(0L);
        blog1.setUserId(0L);
        blog1.setCategoryId(0L);
        blog1.setCategoryName("categoryName");
        blog1.setBlogTags("blogTags");
        blog1.setSubUrl("subUrl");
        final List<Blog> blogs = Arrays.asList(blog1);
        when(mockBlogMapper.selectListByCategoryId(0L)).thenReturn(blogs);

        final List<Blog> result = blogServiceUnderTest.getBlogListByCategoryId(0L, 0, 0);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetBlogListByCategoryId_BlogMapperReturnsNoItems() {

        when(mockBlogMapper.selectListByCategoryId(0L)).thenReturn(Collections.emptyList());

        final List<Blog> result = blogServiceUnderTest.getBlogListByCategoryId(0L, 0, 0);

        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testDeleteBlog() {

        blogServiceUnderTest.deleteBlog(0L);

        verify(mockBlogMapper).deleteByPrimaryKey(0L);
    }
}
