package cn.bincker.modules.tool.service;

import cn.bincker.modules.tool.entity.Tool;

import java.util.List;

public interface ToolService {
    List<Tool> getAll();

    boolean exists(String path);
}
