package com.example.blog_system.model.domain;

import java.util.Date;

public class Comment {
    private Integer id;
    private Integer articleId;
    private String content;
    private Date created;
    private String author;
    private String ip;
    private String status = "not_audit"; // 默认状态为未审核

    // 构造器
    public Comment() {
        this.created = new Date(); // 默认创建时间为当前时间
    }

    // getter和setter方法
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getArticleId() { return articleId; }
    public void setArticleId(Integer articleId) { this.articleId = articleId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getCreated() { return created; }
    public void setCreated(Date created) { this.created = created; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", articleId=" + articleId +
                ", content='" + content + '\'' +
                ", created=" + created +
                ", author='" + author + '\'' +
                ", ip='" + ip + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}