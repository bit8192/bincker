package cn.bincker.modules.blog.controller;

import cn.bincker.modules.blog.entity.Blog;
import cn.bincker.modules.blog.service.BlogService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("blog")
@Slf4j
public class BlogController {
    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping
    public String index(Page<Blog> page, String keywords, Model model) {
        blogService.getPage(page, keywords);
        model.addAttribute("page", page);
        return "blog/index";
    }

    @GetMapping(path = "/**/{path:^[\\w?%&_=]+}.md", produces = MediaType.TEXT_HTML_VALUE)
    public String blog(HttpServletRequest httpRequest, Model model) {
        var path = httpRequest.getRequestURI().replaceAll("^/blog/", "");
        var blog = blogService.getByPath(path);
        if (blog.isEmpty()) return "404";
        model.addAttribute("blog", blog.get());
        model.addAttribute("content", blogService.renderBlogContent(blog.get()));
        return "blog/blog";
    }

//    @GetMapping(value = "/**", produces = MediaType.ALL_VALUE)
//    @ResponseBody
//    public void resources(HttpServletRequest request, HttpServletResponse response) {
//        log.debug("handle resource");
////        resourceHandlerMapping.getHandler(request)
//    }

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
