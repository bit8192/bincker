package cn.bincker.modules.blog.handler;

import cn.bincker.modules.blog.service.BlogService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import java.io.File;
import java.util.List;

@Component
public class BlogResourceRequestHandler extends ResourceHttpRequestHandler {
    @Override
    public void afterPropertiesSet() throws Exception {
        setLocationValues(List.of("file:" + BlogService.BLOG_DIR + File.separator));
        setOptimizeLocations(false);
        setUseLastModified(true);
        super.afterPropertiesSet();
    }
}
