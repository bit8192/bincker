package cn.bincker.modules.blog.service;

import cn.bincker.modules.blog.entity.Blog;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface BlogService {
    void sync();

    IPage<Blog> getPage(Page<Blog> page);

    void hit(Long id);

    void share(Long id);
}
