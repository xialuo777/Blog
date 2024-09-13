package com.blog.service;

import cn.hutool.core.collection.CollectionUtil;
import com.blog.bo.BlogCommentBo;
import com.blog.dto.PageRequest;
import com.blog.entity.BlogComment;
import com.blog.mapper.BlogCommentMapper;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommentService {
    private final BlogCommentMapper blogCommentMapper;
    public void addComment(BlogComment blogComment) {
        blogCommentMapper.insertSelective(blogComment);
    }

    public Optional<BlogComment> selectCommentById(Integer commentId) {
        return Optional.ofNullable(blogCommentMapper.selectByPrimaryKey(commentId));
    }

    public void deleteComment(Integer commentId) {
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
        List<BlogCommentBo> list = addAllNodes(firstCommentList, secondCommentList);
        return list;
    }


    private List<BlogCommentBo> addAllNodes(List<BlogCommentBo> firstCommentList, List<BlogCommentBo> secondCommentList) {
        List<BlogCommentBo> tempSecondList = new ArrayList<>(secondCommentList);

        for (BlogCommentBo comment : tempSecondList) {
            if (addNode(firstCommentList, comment)) {
                secondCommentList.remove(comment);
            }
        }

        return firstCommentList;
    }

    private boolean addNode(List<BlogCommentBo> firstCommentList, BlogCommentBo blogCommentBo) {
        for (BlogCommentBo commentBo : firstCommentList) {
            //判断该回复是否是当前评论的回复，是当前评论的回复，则在其下一节点添加
            if (commentBo.getCommentId().equals(blogCommentBo.getLastId())){
                commentBo.getNextNodes().add(blogCommentBo);
                return true;
            }else {
                //若不是当前评论的回复，则判断其下一节点是否为空，若不为空，则递归判断
                if (CollectionUtil.isNotEmpty(commentBo.getNextNodes())){
                    if (addNode(commentBo.getNextNodes(), blogCommentBo)){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
