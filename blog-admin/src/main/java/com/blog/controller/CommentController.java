package com.blog.controller;

import cn.hutool.core.bean.BeanUtil;
import com.blog.authentication.CurrentUserHolder;
import com.blog.util.bo.BlogCommentBo;
import com.blog.util.dto.PageRequest;
import com.blog.entity.Blog;
import com.blog.entity.BlogComment;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.exception.ResponseResult;
import com.blog.service.BlogService;
import com.blog.service.CommentService;
import com.blog.service.UserService;
import com.blog.vo.comment.CommentInVo;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author: zhang
 * @time: 2024-09-14 10:37
 */
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final UserService userService;
    private final BlogService blogService;
    private final CurrentUserHolder currentUserHolder;

    public static final String PAGE_NO = "pageNo";
    public static final String PAGE_SIZE = "pageSize";

    /**
     * 添加评论
     * @param commentVo 接收添加的评论信息
     * @return ResponseResult
     * @author zhang
     */
    @PostMapping("/blog/comment")
    public ResponseResult<String> addComment(@RequestBody CommentInVo commentVo) {
        User user = userService.selectUserByUserId(commentVo.getCommentatorId())
                .orElseThrow(()->new BusinessException("用户不存在！"));
        if (user.getStatus() == 1) {
            return ResponseResult.fail("该用户已被封禁，无法评论！");
        }
        Blog blog = blogService.getBlogById(commentVo.getBlogId())
                .orElseThrow(()->new BusinessException("该博客不存在！"));
        if (blog.getEnableComment() == 1) {
            return ResponseResult.fail("该博客已关闭评论功能！");
        }
        BlogComment blogComment = new BlogComment();
        BeanUtil.copyProperties(commentVo, blogComment);
        blogComment.setCommentator(user.getNickName());
        blogComment.setCommentatorId(user.getUserId());
        commentService.addComment(blogComment);
        return ResponseResult.success("评论成功！");
    }

    /**
     * 删除评论
     * @param commentId 评论id
     * @return ResponseResult
     * @author zhang

     */
    @DeleteMapping("/blog/delete")
    public ResponseResult<String> deleteComment(Integer commentId) {
        BlogComment blogComment = commentService.selectCommentById(commentId)
                .orElseThrow(()->new BusinessException("该评论不存在！"));
        Long commentatorId = blogComment.getCommentatorId();
        if (!currentUserHolder.getUserId().equals(commentatorId)) {
           return ResponseResult.fail("没有权限删除！");
        }
        commentService.deleteComment(commentId);
        return ResponseResult.success("删除成功！");
    }

    /**
     * 查看当前博客下的所有评论
     * @param blogId 当前博客id
     * @param params 分页信息
     * @return ResponseResult
     * @author zhang
     */

    @GetMapping("/{blogId}")
    public ResponseResult<List<BlogCommentBo>> getCommentListAll(@PathVariable Long blogId, @RequestParam Map<String,Object> params) {
        if (ObjectUtils.isEmpty(params.get(PAGE_NO)) || ObjectUtils.isEmpty(params.get(PAGE_SIZE))) {
            return ResponseResult.fail("参数异常！");
        }
        blogService.getBlogById(blogId).orElseThrow(() -> new BusinessException("该博客不存在！"));
        PageRequest pageRequest = new PageRequest(params);
        return ResponseResult.success(commentService.queryCommentList(pageRequest,blogId));
    }

}
