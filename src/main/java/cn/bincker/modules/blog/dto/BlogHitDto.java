package cn.bincker.modules.blog.dto;

import lombok.Data;

@Data
public class BlogHitDto {
    private Type type;
    public enum Type{
        VIEW,
        LIKE,
        SHARE
    }
}
