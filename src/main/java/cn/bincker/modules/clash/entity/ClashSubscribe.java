package cn.bincker.modules.clash.entity;

import cn.bincker.common.annotation.DDL;
import cn.bincker.common.entity.BaseEntity;
import cn.bincker.modules.clash.entity.config.ClashConfig;
import cn.bincker.modules.clash.handler.ClashConfigTypeHandler;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;

import java.util.Date;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@DDL("""
create table if not exists clash_subscribe(
    id integer primary key autoincrement,
    created_time timestamp default current_timestamp,
    updated_time timestamp default current_timestamp,
    deleted boolean default 0,
    name varchar(64) not null,
    url varchar(1024) not null,
    skip_proxies integer,
    latest_update_time timestamp default current_timestamp,
    latest_content text,
    latest_delay text,
    status tinyint(1) not null default 0,
    download_traffic bigint default 0,
    upload_traffic bigint default 0,
    total_traffic bigint default 0,
    expired_time timestamp
);
""")
@TableName(value = "clash_subscribe", autoResultMap = true)
public class ClashSubscribe extends BaseEntity {
    private String name;
    private String url;
    private Integer skipProxies;
    private Date latestUpdateTime;
    @TableField(typeHandler = ClashConfigTypeHandler.class)
    private ClashConfig latestContent;
    @TableField(typeHandler = JacksonTypeHandler.class, javaType = true)
    private Map<String, Integer> latestDelay;
    @TableField(typeHandler = EnumOrdinalTypeHandler.class, javaType = true)
    private Status status;
    private Long downloadTraffic;
    private Long uploadTraffic;
    private Long totalTraffic;
    private Date expiredTime;

    public enum Status {
        NORMAL("正常"),
        ABNORMAL("异常"),
        ;
        @Getter
        private final String title;

        Status(String title) {
            this.title = title;
        }
    }

    public Integer getMinDelay() {
        if (latestDelay == null) return null;
        return latestDelay.values().stream().min(Integer::compareTo).orElse(null);
    }

    public Integer getMaxDelay() {
        if (latestDelay == null) return null;
        return latestDelay.values().stream().max(Integer::compareTo).orElse(null);
    }

    public Integer getProxyCount() {
        if (latestContent == null) return 0;
        if (latestContent.getProxies() == null) return 0;
        return latestContent.getProxies().size();
    }
}
