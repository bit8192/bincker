package cn.bincker.modules.tool.entity;

import lombok.Data;

import java.util.List;

@Data
public class Tool {
    private String name;
    private String path;
    private String description;
    private List<String> tags;
}
