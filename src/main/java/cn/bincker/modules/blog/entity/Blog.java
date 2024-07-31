package cn.bincker.modules.blog.entity;

import cn.bincker.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@TableName("blog")
@Getter
@Setter
public class Blog extends BaseEntity {
    private String title;

    private Integer sort;

    private Integer hits;

    private Integer shares;
}
