package com.example.blog_system.web.admin;

import com.example.blog_system.model.ResponseData.ArticleResponseData;
import com.example.blog_system.model.domain.Comment;
import com.example.blog_system.service.ICommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/admin/comments")
public class CommentAdminController {

    private static final Logger logger = LoggerFactory.getLogger(CommentAdminController.class);

    private final ICommentService commentService;

    // 使用构造器注入替代字段注入
    public CommentAdminController(ICommentService commentService) {
        this.commentService = commentService;
    }

    // 评论审核页面
    @GetMapping("/audit")
    public String commentAudit(HttpServletRequest request) {
        List<Comment> pendingComments = commentService.getPendingComments();
        request.setAttribute("pendingComments", pendingComments);
        return "back/comment_audit";
    }

    // 审核通过评论
    @PostMapping("/approve")
    @ResponseBody
    public ArticleResponseData<String> approveComment(@RequestParam Integer commentId) {
        try {
            commentService.auditComment(commentId, "approved");
            logger.info("评论审核通过，评论ID: {}", commentId);
            return ArticleResponseData.ok("评论审核通过");
        } catch (Exception e) {
            logger.error("评论审核失败，错误信息: {}", e.getMessage());
            return ArticleResponseData.fail("评论审核失败");
        }
    }

    // 审核拒绝评论
    @PostMapping("/reject")
    @ResponseBody
    public ArticleResponseData<String> rejectComment(@RequestParam Integer commentId) {
        try {
            commentService.auditComment(commentId, "rejected");
            logger.info("评论审核拒绝，评论ID: {}", commentId);
            return ArticleResponseData.ok("评论已拒绝");
        } catch (Exception e) {
            logger.error("评论拒绝失败，错误信息: {}", e.getMessage());
            return ArticleResponseData.fail("评论拒绝失败");
        }
    }

    // 删除评论
    @PostMapping("/delete")
    @ResponseBody
    public ArticleResponseData<String> deleteComment(@RequestParam Integer commentId) {
        try {
            commentService.deleteComment(commentId);
            logger.info("删除评论成功，评论ID: {}", commentId);
            return ArticleResponseData.ok("评论删除成功");
        } catch (Exception e) {
            logger.error("删除评论失败，错误信息: {}", e.getMessage());
            return ArticleResponseData.fail("删除评论失败");
        }
    }

    // 评论管理页面（所有评论）
    @GetMapping("")
    public String commentManagement(HttpServletRequest request) {
        List<Comment> pendingComments = commentService.getPendingComments();
        request.setAttribute("pendingComments", pendingComments);
        return "back/comment_management";
    }
}