package cn.bincker.modules.clash.entity;

import cn.bincker.common.DDL;
import cn.bincker.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@DDL("""
drop table if exists clash_subscribe;
create table if not exists clash_subscribe(
    id integer primary key autoincrement,
    created_time timestamp default current_timestamp,
    updated_time timestamp default current_timestamp,
    deleted boolean default 0,
    name varchar(64) not null,
    url varchar(1024) not null,
    content text,
    skip_proxies integer,
    last_update_time timestamp default current_timestamp,
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
    private String content;
    private Integer skipProxies;
    private Date lastUpdateTime;
    @TableField(typeHandler = EnumOrdinalTypeHandler.class)
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
}
