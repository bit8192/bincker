package cn.bincker.modules.clash.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ClashSubscribeMergeConfigDto {
    private Long id;
    @NotEmpty
    private String name;

    @NotEmpty
    private String overrideConfig;

    @Min(1)
    private Integer limitDelay;

    private String typeRegex;

    private String nameRegex;

    @Pattern(regexp = "^(all|(\\d+)(,\\d+)*,?)$")
    private String subscribeIds;

    @Min(1)
    private Integer intervalMinutes;
}
