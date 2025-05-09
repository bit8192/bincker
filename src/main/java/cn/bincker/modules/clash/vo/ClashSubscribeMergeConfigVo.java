package cn.bincker.modules.clash.vo;

import cn.bincker.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class ClashSubscribeMergeConfigVo extends BaseEntity {
    private Long id;

    private String name;

    private Integer limitDelay;

    private String typeRegex;

    private String nameRegex;

    private Integer intervalMinutes;

    private Date latestMergeTime;

    private Long downloadTraffic;

    private Long uploadTraffic;

    private Long totalTraffic;
}
