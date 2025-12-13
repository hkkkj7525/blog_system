package com.example.blog_system.web.client;

import com.example.blog_system.model.domain.Comment;
import com.example.blog_system.service.ICommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private ICommentService commentService;

    // 添加评论
    @PostMapping
    public ResponseEntity<?> addComment(@RequestBody CommentRequest commentRequest,
                                        HttpServletRequest request,
                                        Authentication authentication) {
        try {
            Comment comment = new Comment();
            comment.setArticleId(commentRequest.getArticleId());
            comment.setContent(commentRequest.getContent());
            comment.setAuthor(authentication.getName());
            comment.setIp(getClientIpAddress(request));
            comment.setStatus("not_audit"); // 设置为待审核状态

            commentService.pushComment(comment);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "评论提交成功，等待管理员审核");
            response.put("comment", comment);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("评论提交失败: " + e.getMessage());
        }
    }

    // 获取文章的已审核评论
    @GetMapping("/article/{articleId}")
    public ResponseEntity<List<Comment>> getArticleComments(@PathVariable Integer articleId) {
        List<Comment> comments = commentService.getApprovedComments(articleId);
        return ResponseEntity.ok(comments);
    }

    // 获取客户端IP地址
    private String getClientIpAddress(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}

class CommentRequest {
    private Integer articleId;
    private String content;

    public Integer getArticleId() { return articleId; }
    public void setArticleId(Integer articleId) { this.articleId = articleId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}