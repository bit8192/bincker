package cn.bincker.modules.clash.dto;

import lombok.Data;

@Data
public class ClashSubscribeMergeConfigDto {
    private String name;

    private String overrideConfig;

    private Integer limitDelay;

    private String typeRegex;

    private String nameRegex;

    private String subscribeIds;
}
