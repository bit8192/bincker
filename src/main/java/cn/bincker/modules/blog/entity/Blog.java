package cn.bincker.modules.blog.entity;

import cn.bincker.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@TableName("blog")
@Getter
@Setter
public class Blog extends BaseEntity {
    private String title;

    private String filePath;

    private Integer sort;

    private Integer hits;

    private Integer shares;

    private Date fileLastModified;
}
