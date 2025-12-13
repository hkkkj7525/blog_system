package com.example.blog_system.service.impl;

import com.example.blog_system.dao.ArticleMapper;
import com.example.blog_system.dao.CommentMapper;
import com.example.blog_system.dao.StatisticMapper;
import com.example.blog_system.model.domain.Article;
import com.example.blog_system.model.domain.Statistic;
import com.example.blog_system.service.IArticleService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ArticleServiceImpl implements IArticleService {

    private final ArticleMapper articleMapper;
    private final StatisticMapper statisticMapper;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final CommentMapper commentMapper;

    @Autowired
    public ArticleServiceImpl(ArticleMapper articleMapper,
                              StatisticMapper statisticMapper,
                              RedisTemplate<Object, Object> redisTemplate,
                              CommentMapper commentMapper) {
        this.articleMapper = articleMapper;
        this.statisticMapper = statisticMapper;
        this.redisTemplate = redisTemplate;
        this.commentMapper = commentMapper;
    }

    @Override
    public void deleteArticleWithId(int id) {
        articleMapper.deleteArticleWithId(id);
        redisTemplate.delete("article_" + id);
        statisticMapper.deleteStatisticWithId(id);
        commentMapper.deleteCommentWithId(id);
    }

    @Override
    public void updateArticleWithId(Article article) {
        article.setModified(new Date());
        articleMapper.updateArticleWithId(article);
        redisTemplate.delete("article_" + article.getId());
    }

    @Override
    public void publish(Article article) {
        article.setContent(EmojiParser.parseToAliases(article.getContent()));
        article.setCreated(new Date());
        article.setHits(0);
        article.setCommentsNum(0);
        articleMapper.publishArticle(article);
        statisticMapper.addStatistic(article);
    }

    @Override
    public Article selectArticleWithId(Integer id) {
        // 避免冗余的null初始化
        Object cachedArticle = redisTemplate.opsForValue().get("article_" + id);
        if (cachedArticle != null) {
            return (Article) cachedArticle;
        }

        Article article = articleMapper.selectArticleWithId(id);
        if (article != null) {
            redisTemplate.opsForValue().set("article_" + id, article);
        }
        return article;
    }

    @Override
    public PageInfo<Article> selectArticleWithPage(Integer page, Integer count) {
        // 使用try-with-resources风格的PageHelper调用
        PageHelper.startPage(page, count);
        try {
            List<Article> articleList = articleMapper.selectArticleWithPage();

            // 使用增强型for循环替代传统for循环
            for (Article article : articleList) {
                Statistic statistic = statisticMapper.selectStatisticWithArticleId(article.getId());
                if (statistic != null) {
                    article.setHits(statistic.getHits());
                    article.setCommentsNum(statistic.getCommentsNum());
                }
            }
            return new PageInfo<>(articleList);
        } finally {
            // PageHelper会自动清理ThreadLocal，这里确保清理
            PageHelper.clearPage();
        }
    }

    @Override
    public List<Article> getHeatArticles() {
        List<Statistic> statistics = statisticMapper.getStatistic();
        List<Article> heatArticles = new ArrayList<>();

        // 使用增强型for循环和计数器
        int count = 0;
        for (Statistic statistic : statistics) {
            if (count >= 10) {
                break;
            }

            Article article = articleMapper.selectArticleWithId(statistic.getArticleId());
            if (article != null) {
                article.setHits(statistic.getHits());
                article.setCommentsNum(statistic.getCommentsNum());
                heatArticles.add(article);
                count++;
            }
        }
        return heatArticles;
    }
}