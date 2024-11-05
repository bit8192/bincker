package cn.bincker.modules.blog.controller;

import cn.bincker.modules.blog.dto.BlogHitDto;
import cn.bincker.modules.blog.entity.Blog;
import cn.bincker.modules.blog.handler.BlogResourceRequestHandler;
import cn.bincker.modules.blog.service.BlogService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
@RequestMapping("blog")
@Slf4j
public class BlogController {
    public static final int AUTHENTICATION_RETRY_TIMES = 10;
    public static final int LOCK_RESOURCE_TIMEOUT = 10 * 60 * 1000;
    private final BlogService blogService;
    private final BlogResourceRequestHandler resourceHandler;
    private final Map<String,AtomicInteger> authenticationFailureCountMap = new ConcurrentHashMap<>();
    private final Map<String, Long> lockedResources = new ConcurrentHashMap<>();

    public BlogController(BlogService blogService, BlogResourceRequestHandler resourceHandler) {
        this.blogService = blogService;
        this.resourceHandler = resourceHandler;
    }

    @GetMapping
    public String index(Page<Blog> page, String keywords, Model model) {
        model.addAttribute("page", blogService.getPage(page, keywords));
        return "blog/index";
    }

    @GetMapping(path = "/**", produces = MediaType.TEXT_HTML_VALUE)
    public String blog(HttpServletRequest request, HttpServletResponse response, Model model) throws ServletException, IOException {
        var path = URLDecoder.decode(request.getRequestURI().replaceAll("^/blog/", ""), StandardCharsets.UTF_8);
        var blogOpt = blogService.getByPath(path);
        if (blogOpt.isEmpty()) {
            if (!blogService.isSafeResource(path)) return "redirect:/404";
            request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, ((String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).replaceAll("^/blog", ""));
            resourceHandler.handleRequest(request, response);
            return null;
        }
        var blog = blogOpt.get();
        if (StringUtils.hasText(blog.getAuthorization())){
            //资源锁定
            var lockedTime = lockedResources.get(path);
            if (lockedTime != null) {
                if (System.currentTimeMillis() < lockedTime){
                    log.error("resources [{}] locked.", path);
                    response.setHeader("WWW-Authenticate", "Basic realm=\"authentication failed.\"");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return "401";
                }
                lockedResources.remove(path);
            }
            var authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (StringUtils.hasText(authorization) && authorization.startsWith("Basic ")){
                authorization = authorization.substring("Basic ".length());
                if (!Objects.equals(new String(Base64.getDecoder().decode(authorization)), blog.getAuthorization())){
                    if (authenticationFailureCountMap.computeIfAbsent(path, (p)->new AtomicInteger()).getAndIncrement() > AUTHENTICATION_RETRY_TIMES){
                        lockedResources.put(path, System.currentTimeMillis() + LOCK_RESOURCE_TIMEOUT);
                    }
                    response.setHeader("WWW-Authenticate", "Basic realm=\"authentication failed.\"");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return "401";
                }
                authenticationFailureCountMap.remove(path);
            }else{
                response.setHeader("WWW-Authenticate", "Basic realm=\"the resources need to be authenticated\"");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return "401";
            }
        }
        model.addAttribute("blog", blogOpt.get());
        model.addAttribute("content", blogService.renderBlogContent(blogOpt.get()));
        return "blog/blog";
    }

    @PostMapping("/**")
    @ResponseBody
    public void hit(HttpServletRequest request, @RequestBody BlogHitDto dto){
        var path = URLDecoder.decode(request.getRequestURI().replaceAll("^/blog/", ""), StandardCharsets.UTF_8);
        if (dto.getType() == BlogHitDto.Type.VIEW) {
            blogService.view(path);
        }else if (dto.getType() == BlogHitDto.Type.LIKE){
            blogService.like(path);
        }else if (dto.getType() == BlogHitDto.Type.SHARE){
            blogService.share(path);
        }
    }
}
