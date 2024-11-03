package cn.bincker.modules.blog.service;

import cn.bincker.modules.blog.entity.Blog;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.nio.file.Path;
import java.util.Optional;

public interface BlogService {
    Path BLOG_DIR = Path.of("blog");
    String DEFAULT_MD_SUFFIX = ".md";
    String META_FILE_SUFFIX = ".meta.json";

    void sync();

    IPage<Blog> getPage(Page<Blog> page, String keywords);

    Optional<Blog> getByPath(String path);

    void view(String path);

    void like(String path);

    void share(String path);

    String renderBlogContent(Blog blog);
}
