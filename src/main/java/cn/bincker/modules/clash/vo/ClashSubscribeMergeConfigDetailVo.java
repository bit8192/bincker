package cn.bincker.modules.clash.vo;

import lombok.Data;

@Data
public class ClashSubscribeMergeConfigDetailVo {
    private String name;

    private String overrideConfig;

    private Integer limitDelay;

    private String typeRegex;

    private String nameRegex;

    private String subscribeIds;

    private String subscribeNames;

    private Integer intervalMinutes;
}
