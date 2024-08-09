package cn.bincker.modules.blog.service.impl;

import cn.bincker.common.exception.SystemException;
import cn.bincker.modules.blog.entity.Blog;
import cn.bincker.modules.blog.mapper.BlogMapper;
import cn.bincker.modules.blog.service.BlogService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;

@Service
@Slf4j
public class BlogServiceImpl implements BlogService, Runnable, ApplicationListener<ApplicationContextEvent> {
    private final BlogMapper blogMapper;
    private final WatchService watchService;
    private final Parser parser;
    private final HtmlRenderer htmlRenderer;
    private boolean running = true;
    private final Map<WatchKey, Path> watchPaths = new HashMap<>();

    public BlogServiceImpl(BlogMapper blogMapper, ThreadPoolTaskExecutor taskExecutor) throws IOException {
        this.blogMapper = blogMapper;
        var option = new MutableDataSet();
        parser = Parser.builder(option).build();
        htmlRenderer = HtmlRenderer.builder(option).build();

        if(!BLOG_DIR.toFile().exists() && !BLOG_DIR.toFile().mkdirs()) throw new SystemException("create blog dir failed");
        watchService = FileSystems.getDefault().newWatchService();
        Files.walkFileTree(BLOG_DIR, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                try {
                    watchPaths.put(dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY), dir);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        taskExecutor.execute(this);
    }

    @Override
    public void run() {
        WatchKey key;
        var updateBlogSet = new HashSet<Path>();
        var deleteBlogSet = new HashSet<Path>();
        while (running) {
            try {
                key = watchService.poll(1, TimeUnit.SECONDS);
                if (key == null) {
                    if (!updateBlogSet.isEmpty()) {
                        updateBlogSet.forEach(p->{
                            try{
                                updateBlogFile(p.toFile());
                            }catch(Exception e){
                                log.error("update blog file error.", e);
                            }
                        });
                        updateBlogSet.clear();
                    }
                    if (!deleteBlogSet.isEmpty()) {
                        deleteBlogSet.forEach(p-> blogMapper.delete(Wrappers.<Blog>lambdaQuery().eq(Blog::getFilePath, BLOG_DIR.relativize(p))));
                        deleteBlogSet.clear();
                    }
                    continue;
                }
            } catch (InterruptedException e) {
                throw new SystemException("watch service error");
            }
            var watchPath = watchPaths.get(key);
            log.debug("watchPath:{}", watchPath);
            if (watchPath != null) for (WatchEvent<?> event : key.pollEvents()) {
                var kind = event.kind();
                if (kind == OVERFLOW) continue;
                var filePath = (Path) event.context();
                var fullPath = watchPath.resolve(filePath);
                if (kind == ENTRY_CREATE) {
                    if(fullPath.toFile().isDirectory()){
                        try {
                            watchPaths.put(fullPath.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY), fullPath);
                        } catch (IOException e) {
                            log.error("register watch path fail: {}", fullPath, e);
                        }
                    }
                }else if(kind == ENTRY_MODIFY){
                    if (isBlogFile(fullPath.toFile())){
                        updateBlogSet.add(fullPath);
                    }
                }else if(kind == ENTRY_DELETE){
                    if (isBlogFile(fullPath.toFile())){
                        deleteBlogSet.add(fullPath);
                    }
                }
                log.debug("event: {}, fullPath: {}", kind.name(), fullPath);
            }
            if(!key.reset()){
                watchPaths.remove(key);
                log.error("file watch key reset fail.");
            }
        }
        try {
            watchService.close();
        } catch (IOException e) {
            log.error("close watch service error", e);
        }
    }

    @Override
    public void onApplicationEvent(@NotNull ApplicationContextEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            sync();
        }else if (event instanceof ContextClosedEvent) {
            running = false;
        }
    }

    @Override
    public void sync() {
        try {
            Files.walkFileTree(BLOG_DIR, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    updateBlogFile(file.toFile());
                    return FileVisitResult.CONTINUE;
                }
            });
        }catch (Exception e){
            log.error("sync error", e);
        }
    }

    private void updateBlogFile(File file) {
        if (!isBlogFile(file)) return;
        String title = null;
        try(var reader = new InputStreamReader(new FileInputStream(file))) {
            var document = parser.parseReader(reader);
            for (Node child : document.getChildren()) {
                if (child instanceof Heading && ((Heading) child).getLevel() == 1){
                    title = ((Heading) child).getText().unescape();
                    break;
                }
            }
        } catch (IOException e) {
            log.warn("parse blog file fail: {}", file, e);
        }
        if (title == null){
            title = getFilenameWithoutSuffix(file.getName()).equalsIgnoreCase("index") ? file.getParentFile().getName() : file.getName();
        }
        var filePath = BLOG_DIR.relativize(file.toPath());
        var blogOpt = blogMapper.selectByFilePathWithoutDeleted(filePath.toString());
        if (blogOpt.isEmpty()){
            var blog = new Blog();
            blog.setTitle(title);
            blog.setHits(0);
            blog.setShares(0);
            blog.setFilePath(filePath.toString());
            blog.setFileLastModified(new Date(file.lastModified()));
            blog.setDeleted(false);
            blogMapper.insert(blog);
        } else {
            var blog = blogOpt.get();
            blog.setTitle(title);
            blog.setFileLastModified(new Date(file.lastModified()));
            blog.setDeleted(false);
            blogMapper.updateBaseInfoWithoutDeleted(blog);
        }
    }

    private static boolean isBlogFile(File file) {
        return !file.isDirectory() && (file.getName().endsWith(".md") || file.getName().endsWith(".MD"));
    }

    private String getFilenameWithoutSuffix(String name) {
        var index = name.lastIndexOf(".");
        if (index < 0) return name;
        return name.substring(0, index);
    }

    @Override
    public IPage<Blog> getPage(Page<Blog> page, String keywords) {
        return blogMapper.selectPage(
                page,
                Wrappers.<Blog>lambdaQuery()
                        .like(keywords != null && !keywords.isBlank(), Blog::getTitle, keywords)
                        .orderByDesc(Blog::getSort)
                        .orderByDesc(Blog::getCreatedTime)
        );
    }

    @Override
    public Optional<Blog> getByPath(String path) {
        if (!path.endsWith(".md") && !path.endsWith(".MD")){
            var blog = blogMapper.selectOne(Wrappers.<Blog>lambdaQuery().eq(Blog::getFilePath, path + ".md"));
            if (blog == null) {
                blog = blogMapper.selectOne(Wrappers.<Blog>lambdaQuery().eq(Blog::getFilePath, path + ".MD"));
            }
            return Optional.ofNullable(blog);
        }else{
            return Optional.ofNullable(blogMapper.selectOne(Wrappers.<Blog>lambdaQuery().eq(Blog::getFilePath, path)));
        }
    }

    @Override
    synchronized public void hit(Long id) {
        var blog = blogMapper.selectById(id);
        if (blog == null) throw new SystemException("博客不存在");
        blogMapper.update(null, Wrappers.<Blog>lambdaUpdate().eq(Blog::getId, id).set(Blog::getHits, blog.getHits() + 1));
    }

    @Override
    public void share(Long id) {
        var blog = blogMapper.selectById(id);
        if (blog == null) throw new SystemException("博客不存在");
        blogMapper.update(null, Wrappers.<Blog>lambdaUpdate().eq(Blog::getId, id).set(Blog::getShares, blog.getShares() + 1));
    }

    @Override
    public String renderBlogContent(Blog blog) {
        try (var reader = new InputStreamReader(new FileInputStream(BLOG_DIR.resolve(blog.getFilePath()).toFile()))) {
            var doc = parser.parseReader(reader);
            return htmlRenderer.render(doc);
        } catch (IOException e) {
            log.error("render blog file fail: {}", blog, e);
            throw new SystemException("渲染内容异常", e);
        }
    }
}
