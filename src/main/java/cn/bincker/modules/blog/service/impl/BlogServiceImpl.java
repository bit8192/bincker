package cn.bincker.modules.blog.service.impl;

import cn.bincker.modules.blog.entity.Blog;
import cn.bincker.modules.blog.mapper.BlogMapper;
import cn.bincker.modules.blog.service.BlogService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@AllArgsConstructor
@Slf4j
public class BlogServiceImpl implements BlogService {
    private static final String BLOG_DIR = "blog";
    private final BlogMapper blogMapper;

    @Override
    public void sync() {
        File blogDir = new File(BLOG_DIR);
        if (!blogDir.exists()) {
            log.warn("blog dir [{}] does not exist", BLOG_DIR);
            return;
        }
        File[] files = blogDir.listFiles();
        if (files == null) return;
        for (File blogFile : files) {
            if (!blogFile.isDirectory() || !blogFile.getName().endsWith(".md") || !blogFile.getName().endsWith(".MD")) continue;
            var title = blogFile.isDirectory() ? blogFile.getName() : getFilenameWithoutSuffix(blogFile.getName());
            if(!blogMapper.exists(Wrappers.<Blog>lambdaQuery().eq(Blog::getTitle, title))){
                var blog = new Blog();
                blog.setTitle(title);
                blog.setHits(0);
                blog.setShares(0);
                blogMapper.insert(blog);
            }
        }
    }

    private String getFilenameWithoutSuffix(String name) {
        var index = name.lastIndexOf(".");
        if (index < 0) return name;
        return name.substring(0, index);
    }

    @Override
    public IPage<Blog> getPage(Page<Blog> page) {
        return null;
    }

    @Override
    public void hit(Long id) {

    }

    @Override
    public void share(Long id) {

    }
}
