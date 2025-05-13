package cn.bincker.modules.clash.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import static cn.bincker.common.constant.RegExpConstant.REGEXP_STR_URL;

@Data
public class ClashSubscribeDto {
    private Long id;
    @NotEmpty
    private String name;
    @NotEmpty
    @Pattern(regexp = REGEXP_STR_URL)
    private String url;
    private Integer skipProxies;
}
