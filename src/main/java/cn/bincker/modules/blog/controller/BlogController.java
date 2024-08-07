package cn.bincker.modules.blog.controller;

import cn.bincker.modules.blog.entity.Blog;
import cn.bincker.modules.blog.handler.BlogResourceRequestHandler;
import cn.bincker.modules.blog.service.BlogService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;

@Controller
@RequestMapping("blog")
@Slf4j
public class BlogController {
    private final BlogService blogService;
    private final BlogResourceRequestHandler resourceHandler;

    public BlogController(BlogService blogService, BlogResourceRequestHandler resourceHandler) {
        this.blogService = blogService;
        this.resourceHandler = resourceHandler;
    }

    @GetMapping
    public String index(Page<Blog> page, String keywords, Model model) {
        blogService.getPage(page, keywords);
        model.addAttribute("page", page);
        return "blog/index";
    }

    @GetMapping(path = "/**", produces = MediaType.TEXT_HTML_VALUE)
    public String blog(HttpServletRequest request, HttpServletResponse response, Model model) throws ServletException, IOException {
        var path = request.getRequestURI().replaceAll("^/blog/", "");
        var blog = blogService.getByPath(path);
        if (blog.isEmpty()) {
            request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, ((String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).replaceAll("^/blog", ""));
            resourceHandler.handleRequest(request, response);
            return null;
        }
        model.addAttribute("blog", blog.get());
        model.addAttribute("content", blogService.renderBlogContent(blog.get()));
        return "blog/blog";
    }

    @PostMapping("/hit/{id}")
    @ResponseBody
    public void hit(@PathVariable Long id){
        blogService.hit(id);
    }

    @PostMapping("/share/{id}")
    @ResponseBody
    public void share(@PathVariable Long id){
        blogService.share(id);
    }
}
