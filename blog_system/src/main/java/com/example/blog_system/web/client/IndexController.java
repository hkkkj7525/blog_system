package com.example.blog_system.web.client;

import com.example.blog_system.model.domain.Article;
import com.example.blog_system.model.domain.Comment;
import com.example.blog_system.service.IArticleService;
import com.example.blog_system.service.ICommentService;
import com.example.blog_system.service.ISiteService;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Classname IndexController
 * @Description 前台首页控制器
 * @Created by CrazyStone
 */
@Controller
public class IndexController {
    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    private final IArticleService articleService;
    private final ICommentService commentService;
    private final ISiteService siteService;

    /**
     * RDM测试页面
     */
    @GetMapping("/rdm-test")
    public String rdmTestPage() {
        return "client/rdm-test";
    }
    /**
     * Redis测试页面
     */
    @GetMapping("/redis-test")
    public String redisTestPage() {
        return "client/redis-test";
    }
    // 使用构造器注入替代字段注入
    @Autowired
    public IndexController(IArticleService articleService,
                           ICommentService commentService,
                           ISiteService siteService) {
        this.articleService = articleService;
        this.commentService = commentService;
        this.siteService = siteService;
    }

    // 文章详情查询
    @GetMapping(value = "/article/{id}")
    public String getArticleById(@PathVariable("id") Integer id, HttpServletRequest request) {
        Article article = articleService.selectArticleWithId(id);
        if (article != null) {
            // 查询封装评论相关数据
            getArticleComments(request, article);
            // 更新文章点击量
            siteService.updateStatistics(article);
            request.setAttribute("article", article);
            return "client/articleDetails";
        } else {
            logger.warn("查询文章详情结果为空，查询文章id: {}", id); // 使用占位符替代字符串拼接
            // 未找到对应文章页面，跳转到提示页
            return "comm/error_404";
        }
    }

    // 查询文章的评论信息，并补充到文章详情里面
    private void getArticleComments(HttpServletRequest request, Article article) {
        if (article.getAllowComment()) {
            // cp表示评论页码，commentPage
            String cp = request.getParameter("cp");
            cp = StringUtils.isBlank(cp) ? "1" : cp;
            request.setAttribute("cp", cp);

            // 只获取已审核的评论
            PageInfo<Comment> comments = commentService.getComments(article.getId(), Integer.parseInt(cp), 3);
            request.setAttribute("comments", comments);
        }
    }

    // 博客首页，会自动跳转到文章页
    @GetMapping(value = "/")
    public String index(HttpServletRequest request) {
        return this.index(request, 1, 5);
    }

    // 文章页
    @GetMapping(value = "/page/{p}")
    public String index(HttpServletRequest request,
                        @PathVariable("p") int page,
                        @RequestParam(value = "count", defaultValue = "5") int count) {
        PageInfo<Article> articles = articleService.selectArticleWithPage(page, count);
        // 获取文章热度统计信息
        List<Article> articleList = articleService.getHeatArticles();
        request.setAttribute("articles", articles);
        request.setAttribute("articleList", articleList);
        logger.info("分页获取文章信息: 页码 {}, 条数 {}", page, count); // 使用占位符
        return "client/index";
    }
}