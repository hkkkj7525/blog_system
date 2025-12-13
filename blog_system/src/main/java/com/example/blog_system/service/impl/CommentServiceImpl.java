package com.example.blog_system.service.impl;

import com.example.blog_system.dao.CommentMapper;
import com.example.blog_system.dao.StatisticMapper;
import com.example.blog_system.model.domain.Comment;
import com.example.blog_system.model.domain.Statistic;
import com.example.blog_system.service.ICommentService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CommentServiceImpl implements ICommentService {

    private final CommentMapper commentMapper;
    private final StatisticMapper statisticMapper;

    // 使用构造器注入
    @Autowired
    public CommentServiceImpl(CommentMapper commentMapper, StatisticMapper statisticMapper) {
        this.commentMapper = commentMapper;
        this.statisticMapper = statisticMapper;
    }


    @Override
    public void pushComment(Comment comment) {
        if (comment.getStatus() == null) {
            comment.setStatus("not_audit");
        }
        commentMapper.pushComment(comment);
    }

    @Override
    public PageInfo<Comment> getComments(Integer aid, int page, int count) {
        PageHelper.startPage(page, count);
        List<Comment> commentList = commentMapper.selectApprovedComments(aid);
        return new PageInfo<>(commentList);
    }

    @Override
    public List<Comment> getPendingComments() {
        return commentMapper.selectPendingComments();
    }

    @Override
    public List<Comment> getApprovedComments(Integer articleId) {
        return commentMapper.selectApprovedComments(articleId);
    }

    @Override
    @Transactional
    public void auditComment(Integer commentId, String status) {
        commentMapper.updateCommentStatus(commentId, status);
        if ("approved".equals(status)) {
            Comment comment = commentMapper.selectCommentById(commentId);
            if (comment != null) {
                Statistic statistic = statisticMapper.selectStatisticWithArticleId(comment.getArticleId());
                if (statistic != null) {
                    statistic.setCommentsNum(statistic.getCommentsNum() + 1);
                    statisticMapper.updateArticleCommentsWithId(statistic);
                }
            }
        }
    }

    @Override
    public Comment getCommentById(Integer id) {
        return commentMapper.selectCommentById(id);
    }

    @Override
    @Transactional
    public void deleteComment(Integer id) {
        Comment comment = commentMapper.selectCommentById(id);
        if (comment != null && "approved".equals(comment.getStatus())) {
            Statistic statistic = statisticMapper.selectStatisticWithArticleId(comment.getArticleId());
            if (statistic != null && statistic.getCommentsNum() > 0) {
                statistic.setCommentsNum(statistic.getCommentsNum() - 1);
                statisticMapper.updateArticleCommentsWithId(statistic);
            }
        }
        commentMapper.deleteCommentById(id);
    }
}