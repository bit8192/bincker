package cn.bincker.modules.blog.service.impl;

import cn.bincker.common.exception.SystemException;
import cn.bincker.modules.blog.entity.Blog;
import cn.bincker.modules.blog.service.BlogService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;

@Service
@Slf4j
public class BlogServiceImpl implements BlogService, ApplicationListener<ApplicationContextEvent> {
    private final WatchService watchService;
    private final Parser parser;
    private final HtmlRenderer htmlRenderer;
    private boolean running = true;
    private final Map<WatchKey, Path> watchPaths = new HashMap<>();
    private final Map<Path, Blog> blogMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final Object metaLock = new Object();
    private final Map<Path, BlogPersistenceObject> persistenceBlogMap = new ConcurrentHashMap<>();

    public BlogServiceImpl(ThreadPoolTaskExecutor taskExecutor, @Qualifier("blogMetaObjectMapper") ObjectMapper objectMapper) throws IOException {
        this.objectMapper = objectMapper;
        var option = new MutableDataSet();
        parser = Parser.builder(option).build();
        htmlRenderer = HtmlRenderer.builder(option).build();

        if(!BLOG_DIR.toFile().exists() && !BLOG_DIR.toFile().mkdirs()) throw new SystemException("create blog dir failed");
        watchService = FileSystems.getDefault().newWatchService();
        Files.walkFileTree(BLOG_DIR, new SimpleFileVisitor<>() {
            @Override
            public @NotNull FileVisitResult preVisitDirectory(Path dir, @NotNull BasicFileAttributes attrs) {
                try {
                    log.debug("watch dir:{}", dir);
                    watchPaths.put(dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY), dir);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        Runnable fileWatcher = () -> {
            WatchKey key;
            var updateBlogSet = new HashSet<Path>();
            var updateBlogMetaSet = new HashSet<Path>();
            var deleteBlogSet = new HashSet<Path>();
            while (running) {
                try {
                    key = watchService.poll(1, TimeUnit.SECONDS);
                    if (key == null) {
                        if (!updateBlogSet.isEmpty()) {
                            updateBlogSet.forEach(p -> {
                                try {
                                    updateBlogFile(p.toFile());
                                } catch (Exception e) {
                                    log.error("update blog file error.", e);
                                }
                            });
                            updateBlogSet.clear();
                        }
                        if (!updateBlogMetaSet.isEmpty()) {
                            updateBlogMetaSet.forEach(p -> {
                                try {
                                    updateBlogMetaFile(BLOG_DIR.relativize(p));
                                } catch (Exception e) {
                                    log.error("update blog meta file error.", e);
                                }
                            });
                            updateBlogSet.clear();
                        }
                        if (!deleteBlogSet.isEmpty()) {
                            deleteBlogSet.forEach(p -> {
                                blogMap.remove(BLOG_DIR.relativize(p));
                                var metaFile = blogPathToMetaFile(BLOG_DIR.relativize(p));
                                if (metaFile.exists() && !metaFile.delete()) {
                                    log.error("delete blog meta file failed: {}", metaFile);
                                }
                            });
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
                        if (fullPath.toFile().isDirectory()) {
                            try {
                                watchPaths.put(fullPath.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY), fullPath);
                            } catch (IOException e) {
                                log.error("register watch path fail: {}", fullPath, e);
                            }
                        } else if (isBlogFile(fullPath.toFile())) {
                            updateBlogFile(fullPath.toFile());
                        } else if (isBlogMetaFile(fullPath.toFile())) {
                            updateBlogMetaSet.add(fullPath);
                        }
                    } else if (kind == ENTRY_MODIFY) {
                        if (isBlogFile(fullPath.toFile())) {
                            updateBlogSet.add(fullPath);
                        } else if (isBlogMetaFile(fullPath.toFile())) {
                            updateBlogMetaSet.add(fullPath);
                        }
                    } else if (kind == ENTRY_DELETE) {
                        if (isBlogFile(fullPath.toFile())) {
                            deleteBlogSet.add(fullPath);
                        }
                    }
                    log.debug("event: {}, fullPath: {}", kind.name(), fullPath);
                }
                if (!key.reset()) {
                    watchPaths.remove(key);
                    log.error("file watch key reset fail.");
                }
            }
            try {
                watchService.close();
            } catch (IOException e) {
                log.error("close watch service error", e);
            }
        };

        taskExecutor.execute(fileWatcher);

        //当有持久化请求后需要再等一分钟，如果这期间没有相同请求再进行持久化
        Runnable metaWriter = () -> {
            while (running) {
                synchronized (metaLock) {
                    try {
                        //加快停止服务速度
                        if (log.isDebugEnabled()) {
                            metaLock.wait(100);
                        }else{
                            //当有持久化请求后需要再等一分钟，如果这期间没有相同请求再进行持久化
                            metaLock.wait();
                            if (!running) break;
                            //noinspection BusyWait
                            Thread.sleep(60000);
                        }
                    } catch (InterruptedException e) {
                        if (running) throw new RuntimeException(e);
                        break;
                    }
                }
                persistenceBlogMap.values().stream().filter(o -> o.time + 60000 < System.currentTimeMillis()).forEach(o -> {
                    persistenceBlogMap.remove(o.blog.getFilePath());
                    writeBlogMeta(blogPathToMetaFile(o.blog.getFilePath()), o.blog);
                });
            }
        };
        taskExecutor.execute(metaWriter);
    }

    @Override
    public void onApplicationEvent(@NotNull ApplicationContextEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            sync();
        }else if (event instanceof ContextClosedEvent) {
            running = false;
            synchronized (metaLock) {
                metaLock.notifyAll();
            }
        }
    }

    @Override
    public void sync() {
        try {
            Files.walkFileTree(BLOG_DIR, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    try {
                        updateBlogFile(file.toFile());
                    } catch (Exception e) {
                        log.error("sync blog error: {}", file, e);
                    }
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
        var blog = blogMap.get(filePath);
        if (blog == null){
            blog = readOrCreateMeta(filePath);
            blog.setTitle(title);
            blogMap.put(filePath, blog);
        } else {
            blog.setTitle(title);
            blog.setFileLastModified(new Date(file.lastModified()));
        }
        persistenceBlogMeta(blog);
    }

    private void updateBlogMetaFile(Path metaPath) {
        File blogFile = metaPathToBlogFile(metaPath);
        if (!blogFile.exists()){
            log.warn("blog file not found on meta file update: metaPath={}", metaPath);
            return;
        }
        var blogPath = BLOG_DIR.relativize(blogFile.toPath());
        var memBlog = blogMap.get(blogPath);
        var metaFile = BLOG_DIR.resolve(metaPath).toFile();
        if (!metaFile.exists()) {
            log.warn("blog meta file not found: metaPath={}", metaPath);
            return;
        }
        Blog blog = readMetaFile(metaFile);
        if (memBlog == null){
            blogMap.put(blogPath, blog);
        }else{
            BeanUtils.copyProperties(blog, memBlog);
        }
    }

    /**
     * 读取或创建Meta文件
     * @param blogPath 博客文件
     */
    private Blog readOrCreateMeta(Path blogPath) {
        File metaFile = blogPathToMetaFile(blogPath);
        Blog blog;
        if (metaFile.exists()){
            blog = readMetaFile(metaFile);
        }else{
            blog = new Blog();
            blog.setFilePath(blogPath);
            blog.setViews(0L);
            blog.setLikes(0L);
            blog.setShares(0L);
            Path absluteBlogPath = BLOG_DIR.resolve(blogPath);
            try {
                var attribute = Files.readAttributes(absluteBlogPath, BasicFileAttributes.class);
                blog.setFileCreatedTime(new Date(attribute.creationTime().toMillis()));
                blog.setFileLastModified(new Date(attribute.lastModifiedTime().toMillis()));
            } catch (IOException e) {
                log.warn("read blog meta file create time failed. file={}", blogPath, e);
                blog.setFileLastModified(new Date(absluteBlogPath.toFile().lastModified()));
            }
        }
        return blog;
    }

    private static @NotNull File blogPathToMetaFile(Path blogPath) {
        var filename = blogPath.getFileName().toString();
        filename = filename.substring(0, filename.indexOf("."));
        var parent = blogPath.getParent() != null ? BLOG_DIR.resolve(blogPath.getParent()) : BLOG_DIR;
        return parent.resolve(filename + META_FILE_SUFFIX).toFile();
    }

    private static @NotNull File metaPathToBlogFile(Path metaPath) {
        var filename = metaPath.getFileName().toString();
        filename = filename.substring(0, filename.indexOf("."));
        File blogFile;
        Path parent = metaPath.getParent() != null ? BLOG_DIR.resolve(metaPath.getParent()) : BLOG_DIR;
        blogFile = parent.resolve(filename + DEFAULT_MD_SUFFIX).toFile();
        if (blogFile.exists()) return blogFile;
        return parent.resolve(filename + DEFAULT_MD_SUFFIX.toUpperCase()).toFile();
    }

    private Blog readMetaFile(File metaFile) {
        Blog blog;
        try {
            blog = objectMapper.readValue(metaFile, Blog.class);
        } catch (IOException e) {
            throw new SystemException("read blog meta failed: " + metaFile, e);
        }
        return blog;
    }

    private void writeBlogMeta(File metaFile, Blog blog) {
        try {
            objectMapper.writeValue(metaFile, blog);
        } catch (IOException e) {
            throw new SystemException("write blog meta file failed. file=" + metaFile, e);
        }
    }

    private static boolean isBlogFile(File file) {
        return !file.isDirectory() && (file.getName().endsWith(DEFAULT_MD_SUFFIX) || file.getName().endsWith(DEFAULT_MD_SUFFIX.toUpperCase()));
    }

    private static boolean isBlogMetaFile(File file) {
        return !file.isDirectory() && file.getName().endsWith(META_FILE_SUFFIX);
    }

    private String getFilenameWithoutSuffix(String name) {
        var index = name.lastIndexOf(".");
        if (index < 0) return name;
        return name.substring(0, index);
    }

    @Override
    public IPage<Blog> getPage(Page<Blog> page, String keywords) {
        List<String> keywordsList = StringUtils.hasText(keywords) ? Arrays.stream(keywords.split("\\s")).filter(StringUtils::hasText).toList() : Collections.emptyList();
        var findItems = blogMap.values()
                .stream()
                .filter(blog->
                        keywordsList.isEmpty() || keywordsList.stream()
                                .anyMatch(k -> blog.getTitle().contains(k) || (StringUtils.hasText(blog.getKeywords()) && blog.getKeywords().contains(k)))
                )
                .sorted((b1,b2)->{
                    if (!Objects.equals(b1.getSort(), b2.getSort())) {
                        if (b1.getSort() != null && b2.getSort() != null) return b1.getSort().compareTo(b2.getSort());
                        if (b1.getSort() == null) return 1;
                        return -1;
                    }
                    var t1 = b1.getFileCreatedTime() == null ? null : b1.getFileCreatedTime().getTime();
                    var t2 = b2.getFileCreatedTime() == null ? null : b2.getFileCreatedTime().getTime();
                    if (!Objects.equals(t1, t2)) {
                        if (t1 != null && t2 != null) return t1.compareTo(t2);
                        if (t1 == null) return 1;
                        return -1;
                    }
                    return 0;
                })
                .toList();
        if (findItems.isEmpty()) return page;
        page.setTotal(findItems.size());
        long skipItems = (page.getCurrent() - 1) * page.getSize();
        if (findItems.size() > skipItems) {
            page.setRecords(findItems.subList((int) skipItems, (int) Math.min(findItems.size(), skipItems + page.getSize())));
        }
        return page;
    }

    @Override
    public Optional<Blog> getByPath(String path) {
        return Optional.ofNullable(blogMap.get(Path.of(path)));
    }

    @Override
    public void view(String path) {
        var blog = getByPath(path).orElseThrow();
        blog.setLikes(blog.getLikes() + 1);
        persistenceBlogMeta(blog);
    }

    @Override
    public void like(String path) {
        var blog = getByPath(path).orElseThrow();
        blog.setLikes(blog.getLikes() + 1);
        persistenceBlogMeta(blog);
    }

    @Override
    public void share(String path) {
        var blog = getByPath(path).orElseThrow();
        blog.setShares(blog.getShares() + 1);
        persistenceBlogMeta(blog);
    }

    private void persistenceBlogMeta(Blog blog){
        var pBlog = persistenceBlogMap.get(blog.getFilePath());
        if (pBlog == null) {
            pBlog = new BlogPersistenceObject(blog, System.currentTimeMillis());
            persistenceBlogMap.put(blog.getFilePath(), pBlog);
        }else {
            pBlog.time = System.currentTimeMillis();
        }
        synchronized (metaLock){
            metaLock.notifyAll();
        }
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

    @Override
    public boolean isSafeResource(String path) {
        return !path.endsWith(DEFAULT_MD_SUFFIX) && !path.endsWith(DEFAULT_MD_SUFFIX.toUpperCase()) && !path.endsWith(META_FILE_SUFFIX);
    }

    @AllArgsConstructor
    private static class BlogPersistenceObject{
        private Blog blog;
        private long time;
    }
}
