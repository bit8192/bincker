package cn.bincker.modules.clash.entity;

import cn.bincker.common.DDL;
import cn.bincker.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@DDL("""
create table if not exists clash_subscribe_merge_config(
    id integer primary key autoincrement,
    created_time timestamp default current_timestamp,
    updated_time timestamp default current_timestamp,
    deleted boolean default 0,
    name varchar(255) not null,
    token varchar(32) not null unique,
    override_config text,
    limit_delay int,
    type_regex varchar(64),
    name_regex varchar(64),
    subscribe_ids varchar(255),
    interval_minutes int,
    latest_merge_time timestamp,
    latest_merge_content text
);
""")
public class ClashSubscribeMergeConfig extends BaseEntity {
    private String name;

    @JsonIgnore
    private String token;

    private String overrideConfig;

    private Integer limitDelay;

    private String typeRegex;

    private String nameRegex;

    private String subscribeIds;

    private Integer intervalMinutes;

    private Date latestMergeTime;

    private String latestMergeContent;
}
