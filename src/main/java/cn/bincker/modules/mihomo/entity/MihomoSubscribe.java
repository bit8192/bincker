package cn.bincker.modules.mihomo.entity;

import cn.bincker.common.DDL;
import cn.bincker.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@DDL("""
create table if not exists mihomo_subscribe(
    id integer primary key autoincrement,
    created_time timestamp default current_timestamp,
    updated_time timestamp default current_timestamp,
    deleted boolean default 0,
    name varchar(64) not null,
    url varchar(1024) not null,
    content text,
    last_update_time timestamp default current_timestamp,
    status tinyint(1) not null default 0
);
""")
public class MihomoSubscribe extends BaseEntity {
    private String name;
    private String url;
    private String content;
    private Date lastUpdateTime;
    private Status status;

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
