package com.example.blog_system.service;

import com.example.blog_system.model.domain.Comment;
import com.github.pagehelper.PageInfo;
import java.util.List;

public interface ICommentService {
    // 获取文章下的评论（分页）
    PageInfo<Comment> getComments(Integer aid, int page, int count);

    // 用户发表评论
    void pushComment(Comment comment);

    // 获取待审核的评论
    List<Comment> getPendingComments();

    // 获取已审核的评论
    List<Comment> getApprovedComments(Integer articleId);

    // 审核评论
    void auditComment(Integer commentId, String status);

    // 根据ID获取评论
    Comment getCommentById(Integer id);

    // 删除评论
    void deleteComment(Integer id);
}