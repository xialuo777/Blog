package com.blog.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.blog.authentication.CurrentUserHolder;
import com.blog.entity.Blog;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.exception.ResponseResult;
import com.blog.service.BlogService;
import com.blog.service.UserService;
import com.blog.util.dto.PageRequest;
import com.blog.util.dto.PageResult;
import com.blog.vo.blog.BlogDesc;
import com.blog.vo.blog.BlogDetail;
import com.blog.vo.blog.BlogInVo;
import com.blog.vo.blog.BlogUpdateVo;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: zhang
 * @time: 2024-09-14 10:34
 */
@RestController
@RequestMapping("/blogs")
@RequiredArgsConstructor
public class BlogController extends BaseController{
    private final CurrentUserHolder currentUserHolder;

    private final UserService userService;
    private final BlogService blogService;

    /**
     * 保存文章
     * @param blogInVo 接收文章信息
     * @return ResponseResult
     * @author zhang
     */
    @PostMapping("/save")
    public ResponseResult<String> saveBlog(@RequestBody BlogInVo blogInVo) {
        User user = userService.selectUserByUserId(currentUserHolder.getUserId())
                .orElseThrow(() -> new BusinessException("用户不存在！"));
        if (!validUserStatus(user)){
            return ResponseResult.fail("该用户已被封禁，无法发布文章！");
        }
        Blog blog = new Blog();
        BeanUtil.copyProperties(blogInVo, blog);
        blogService.saveBlog(blog);
        return ResponseResult.success("文章保存成功");
    }

    /**
     * 根据博客id更新博客
     * @param blogUpdateVo 接收更新添加的文章信息
     * @param blogId 接收更新的文章id
     * @return ResponseResult
     * @author zhang
     */
    @PostMapping("/update/{blogId}")
    public ResponseResult<String> updateBlog(@RequestBody BlogUpdateVo blogUpdateVo, @PathVariable Long blogId) {
        Blog blog = blogService.getBlogById(blogId)
                .orElseThrow(() -> new BusinessException("文章不存在！"));
        if (!currentUserHolder.getUserId().equals(blog.getUserId())){
            return ResponseResult.fail("您没有权限修改该文章！");
        }
        User user = userService.selectUserByUserId(blog.getUserId()).orElseThrow(()->new BusinessException("用户不存在！"));
        if (!validUserStatus(user)){
            return ResponseResult.fail("该用户已被封禁，无法修改文章！");
        }
        BeanUtil.copyProperties(blogUpdateVo, blog, CopyOptions.create().setIgnoreNullValue(true).setIgnoreCase(true));
        blogService.updateBlog(blog);
        return ResponseResult.success("文章更新成功");
    }

    /**
     * 获取当前用户的博客列表
     * @param params 接收分页信息
     * @return ResponseResult
     * @author zhang
     */
    @GetMapping("/list")
    public ResponseResult<PageResult<BlogDesc>> getCurrentUserBlogList(@RequestParam Map<String, Object> params) {
        if (!validPageParams(params)){
            return ResponseResult.fail("分页参数异常");
        }
        PageRequest pageRequest = new PageRequest(params);
        Long userId = currentUserHolder.getUserId();
        List<Blog> blogList = blogService.getBlogListByUserId(userId, pageRequest.getPageNo(), pageRequest.getPageSize());
        if (CollectionUtils.isEmpty(blogList)) {
            return ResponseResult.fail("当前用户博客列表为空");
        }
        List<BlogDesc> blogDescList = blogList.stream()
                .map(blog -> BeanUtil.copyProperties(blog, BlogDesc.class))
                .collect(Collectors.toList());
        int totalCount = blogList.size();
        PageResult<BlogDesc> pageResult = new PageResult<>(blogDescList, totalCount);
        return ResponseResult.success(pageResult);
    }

    /**
     * 获取博客详情
     * @param blogId 接收博客id
     * @return ResponseResult
     * @author zhang
     */
    @GetMapping("/detail/{blogId}")
    public ResponseResult<BlogDetail> getBlog(@PathVariable Long blogId) {
        Blog blog = blogService.getBlogById(blogId)
                .orElseThrow(() -> new BusinessException("文章不存在！"));
        BlogDetail blogDetail = new BlogDetail();
        BeanUtil.copyProperties(blog, blogDetail);
        return ResponseResult.success(blogDetail);
    }

    /**
     * 根据分类id获取博客列表
     * @param categoryId 接收查询分类id
     * @param params 接收分页信息
     * @return ResponseResult
     * @author zhang
     */
    @GetMapping("/category/{categoryId}")
    public ResponseResult<List<BlogInVo>> getBlogListByCategoryId(@PathVariable Long categoryId, @RequestParam Map<String, Object> params) {
        if (!validPageParams(params)){
            return ResponseResult.fail("分页参数异常");
        }
        PageRequest pageRequest = new PageRequest(params);
        List<Blog> blogList = blogService.getBlogListByCategoryId(categoryId, pageRequest.getPageNo(), pageRequest.getPageSize());
        if (CollectionUtils.isEmpty(blogList)) {
            return ResponseResult.fail("该分类下暂无文章");
        }
        List<BlogInVo> blogInVoList = blogList.stream()
                .map(blog -> BeanUtil.copyProperties(blog, BlogInVo.class))
                .collect(Collectors.toList());
        return ResponseResult.success(blogInVoList);
    }

    /**
     * 根据博客id删除博客
     * @param blogId 接收删除博客的id信息
     * @return ResponseResult
     * @author zhang
     */
    @PostMapping("/delete/{blogId}")
    public ResponseResult<String> deleteBlog(@PathVariable Long blogId) {
        Blog blog = blogService.getBlogById(blogId)
                .orElseThrow(() -> new BusinessException("文章不存在！"));
        if (!currentUserHolder.getUserId().equals(blog.getUserId())){
            return ResponseResult.fail("您没有权限删除该文章！");
        }
        blogService.deleteBlog(blogId);
        return ResponseResult.success("删除成功");
    }

    /**
     * 获取所有博客列表
     * @param params 接受列表查询的分页信息
     * @return ResponseResult
     */
    @GetMapping("/blog/list")
    public ResponseResult<List<BlogDesc>> getBlogList(@RequestParam Map<String, Object> params) {
        if (!validPageParams(params)){
            return ResponseResult.fail("分页参数异常");
        }
        PageRequest pageRequest = new PageRequest(params);
        List<BlogDesc> blogDescList = blogService.getBlogList(pageRequest.getPageNo(), pageRequest.getPageSize())
                .orElseThrow(() -> new BusinessException("博客列表为空"));
        return ResponseResult.success(blogDescList);
    }

}
