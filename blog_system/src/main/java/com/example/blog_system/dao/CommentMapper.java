package com.example.blog_system.dao;

import com.example.blog_system.model.domain.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {
    @Select("SELECT * FROM t_comment WHERE article_id=#{aid} ORDER BY id DESC")
    List<Comment> selectCommentWithPage(Integer aid);

    @Select("SELECT * FROM t_comment ORDER BY id DESC")
    List<Comment> selectNewComment();

    @Insert("INSERT INTO t_comment (article_id, created, author, ip, content, status) " +
            "VALUES (#{articleId}, #{created}, #{author}, #{ip}, #{content}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void pushComment(Comment comment);

    @Select("SELECT COUNT(1) FROM t_comment")
    Integer countComment();

    @Delete("DELETE FROM t_comment WHERE article_id=#{aid}")
    void deleteCommentWithId(Integer aid);

    // 获取待审核的评论
    @Select("SELECT * FROM t_comment WHERE status='not_audit' ORDER BY id DESC")
    List<Comment> selectPendingComments();

    // 获取已审核的评论
    @Select("SELECT * FROM t_comment WHERE article_id=#{articleId} AND status='approved' ORDER BY id DESC")
    List<Comment> selectApprovedComments(Integer articleId);

    // 更新评论状态
    @Update("UPDATE t_comment SET status=#{status} WHERE id=#{commentId}")
    void updateCommentStatus(@Param("commentId") Integer commentId, @Param("status") String status);

    // 根据ID获取评论
    @Select("SELECT * FROM t_comment WHERE id=#{id}")
    Comment selectCommentById(Integer id);

    // 删除评论
    @Delete("DELETE FROM t_comment WHERE id=#{id}")
    void deleteCommentById(Integer id);
}