package com.example.blog_system.service;


import com.example.blog_system.model.ResponseData.StaticticsBo;
import com.example.blog_system.model.domain.Article;
import com.example.blog_system.model.domain.Comment;

import java.util.List;
/**
 * @Classname ISiteService
 * @Description 博客站点统计服务
 * @Created by CrazyStone
 */
public interface ISiteService {
    // 最新收到的评论
    public List<Comment> recentComments(int count);

    // 最新发表的文章
    public List<Article> recentArticles(int count);

    // 获取后台统计数据
    public StaticticsBo getStatistics();

    // 更新某个文章的统计数据
    public void updateStatistics(Article article);
}

