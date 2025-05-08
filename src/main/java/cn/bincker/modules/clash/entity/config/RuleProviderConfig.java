package cn.bincker.modules.clash.entity.config;

import lombok.Data;
import java.util.List;

@Data
public class RuleProviderConfig {
    private String type;
    private String behavior;
    private Integer interval;
    private String path;
    private String url;
    private String proxy;
    private Integer sizeLimit;
    private String format;
    private List<String> payload;
} 