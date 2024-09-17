package com.blog.service;

import cn.hutool.core.collection.CollectionUtil;
import com.blog.exception.BusinessException;
import com.blog.util.bo.BlogCommentBo;
import com.blog.util.dto.PageRequest;
import com.blog.entity.BlogComment;
import com.blog.mapper.BlogCommentMapper;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author: zhang
 * @time: 2024-09-14 12:47
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CommentService {
    private final BlogCommentMapper blogCommentMapper;
    public void addComment(BlogComment blogComment) {
        blogCommentMapper.insertSelective(blogComment);
    }

    public Optional<BlogComment> selectCommentById(Long commentId) {
        return Optional.ofNullable(blogCommentMapper.selectByPrimaryKey(commentId));
    }

    public void deleteComment(Long commentId) {
        blogCommentMapper.deleteByPrimaryKey(commentId);
    }
    public List<BlogCommentBo> queryCommentList(PageRequest pageRequest, Long blogId) {
        int pageSize = pageRequest.getPageSize();
        int pageNo = pageRequest.getPageNo();
        PageHelper.startPage(pageNo, pageSize);
        //所有一级评论集合
        List<BlogCommentBo> firstCommentList = blogCommentMapper.queryFirstCommentList(blogId);
        //其他所有评论集合
        List<BlogCommentBo> secondCommentList = blogCommentMapper.querySecondCommentList(blogId);
        //将所有的其他评论以链表的方式添加到一级评论
        return addAllNodes(firstCommentList, secondCommentList);
    }


    private List<BlogCommentBo> addAllNodes(List<BlogCommentBo> firstCommentList, List<BlogCommentBo> secondCommentList) {
       while (CollectionUtil.isNotEmpty(secondCommentList)){
           for (int i = secondCommentList.size() - 1; i >= 0; i--) {
               if (addNode(firstCommentList, secondCommentList.get(i))) {
                   secondCommentList.remove(i);
               }
           }
       }
       return firstCommentList;
    }

    private boolean addNode(List<BlogCommentBo> firstCommentList, BlogCommentBo blogCommentBo) {
        for (BlogCommentBo commentBo : firstCommentList) {
            //判断该回复是否是当前评论的回复，是当前评论的回复，则在其下一个节点添加
            if (commentBo.getCommentId().equals(blogCommentBo.getLastId())){
                commentBo.getNextNodes().add(blogCommentBo);
                return true;
                //若不是当前评论的回复，则判断其下一个节点是否为空，若不为空，则递归判断
            }else if (CollectionUtil.isNotEmpty(commentBo.getNextNodes())){
                    if (addNode(commentBo.getNextNodes(), blogCommentBo)){
                        return true;
                    }
                }
            }
        return false;
    }
}
