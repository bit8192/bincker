package cn.bincker.modules.blog.service;

import cn.bincker.modules.blog.entity.Blog;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.nio.file.Path;
import java.util.Optional;

public interface BlogService {
    Path BLOG_DIR = Path.of("blog");

    void sync();

    IPage<Blog> getPage(Page<Blog> page, String keywords);

    Optional<Blog> getByPath(String path);

    void hit(Long id);

    void share(Long id);

    String renderBlogContent(Blog blog);
}
