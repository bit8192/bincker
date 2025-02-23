package cn.bincker.modules.tool.service.impl;

import cn.bincker.common.exception.SystemException;
import cn.bincker.modules.tool.entity.Tool;
import cn.bincker.modules.tool.service.ToolService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ToolServiceImpl implements ToolService {
    public static final String TEMPLATE_PATH = "/templates/";
    public static final String TOOL_PATH = TEMPLATE_PATH + "tool/";
    private final List<Tool> tools;
    private final ObjectMapper objectMapper;

    public ToolServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.tools = initTools();
    }

    private List<Tool> initTools() {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        String resourcePath = "classpath*:" + TOOL_PATH + "**/*.html";
        Resource[] resources;
        List<Tool> tools = new ArrayList<>();
        try {
            resources = resolver.getResources(resourcePath);
            for (Resource resource : resources) {
                try(var reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                    var uri = resource.getURI();
                    var path = uri.getPath();
                    path = path.substring(path.indexOf(TEMPLATE_PATH) + TEMPLATE_PATH.length());
                    if (path.equals("tool/index.html")) continue;//剔除首页
                    Tool tool = parseTool(reader.readLine());
                    tool.setPath(path);
                    log.debug("found tool:{}\t{}", path, tool);
                    tools.add(tool);
                } catch (Exception e) {
                    log.error("read tool resource error. source={}", resource.getURI(), e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tools;
    }

    private Tool parseTool(String s) {
        if (s == null || s.isEmpty())
            throw new SystemException("tool description is empty");
        s = s.trim();
        if (!s.startsWith("<!--") || !s.endsWith("-->")) throw new SystemException("invalid tool description");
        s = s.substring(4, s.length() - 3);
        try {
            return objectMapper.readValue(s, Tool.class);
        } catch (JsonProcessingException e) {
            throw new SystemException("invalid tool description: " + s);
        }
    }

    @Override
    public List<Tool> getAll() {
        return tools;
    }

    @Override
    public boolean exists(String path) {
        return tools.stream().anyMatch(tool -> tool.getPath().equals(path));
    }
}
