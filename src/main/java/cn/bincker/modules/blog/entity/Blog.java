package cn.bincker.modules.blog.entity;

import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class Blog {
    private String title;

    private List<String> tags;

    private String keywords;

    private Path filePath;

    private Integer sort;

    private Long views;

    private Long likes;

    private Long shares;

    private Date fileLastModified;

    private Date fileCreatedTime;

    /**
     * 认证
     */
    private String authorization;

    synchronized public void setViews(Long views) {
        this.views = views;
    }

    synchronized public void setLikes(Long likes) {
        this.likes = likes;
    }

    synchronized public void setShares(Long shares) {
        this.shares = shares;
    }
}
