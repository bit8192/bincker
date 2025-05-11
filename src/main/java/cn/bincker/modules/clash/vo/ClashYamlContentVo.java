package cn.bincker.modules.clash.vo;

import lombok.Data;

@Data
public class ClashYamlContentVo {
    private String content;
    private Long downloadTraffic;
    private Long uploadTraffic;
    private Long totalTraffic;
    private Long expire;
}
