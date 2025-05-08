package cn.bincker.modules.clash.dto;

import lombok.Data;

@Data
public class ClashSubscribeDto {
    private String name;
    private String url;
    private Integer skipProxies;
}
