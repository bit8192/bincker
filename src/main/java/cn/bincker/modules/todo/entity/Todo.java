package cn.bincker.modules.todo.entity;

import cn.bincker.common.annotation.DDL;
import cn.bincker.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "todo",autoResultMap = true)
@DDL("""
        create table if not exists todo(
        id integer primary key autoincrement,
        created_time timestamp default current_timestamp,
        updated_time timestamp default current_timestamp,
        deleted boolean default 0,
        title varchar(255) default null,
        content text default null,
        priority integer default null,
        status int default null,
        parent_id bigint(20) default null,
        cyclic boolean default null,
        cycle varchar(255) default null,
        completed_count int default 0,
        cancelled_count int default 0,
        procrastination_count int default 0,
        start_time timestamp default null,
        end_time timestamp default null,
        is_chinese_calendar boolean default false,
        consumed_time unsigned bigint(20) default null
        );
        """)
public class Todo extends BaseEntity {
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String content;
    /**
     * 优先级
     */
    private Integer priority;
    /**
     * 状态
     */
    private Status status;
    /**
     * 父级ID
     */
    private Long parentId;
    /**
     * 是否循环
     */
    private Boolean cyclic;
    /**
     * 循环时间
     * n h(小时)/d(天)/w(周)/m(月)/y(年)
     */
    private String cycle;
    /**
     * 取消次数
     */
    private Integer cancelledCount;
    /**
     * 完成次数
     */
    private Integer completedCount;
    /**
     * 拖延次数
     */
    private Integer procrastinationCount;
    /**
     * 开始时间
     */
    private Date startTime;
    /**
     * 结束时间
     */
    private Date endTime;
    /**
     * 是否为农历时间
     */
    private Boolean isChineseCalendar;
    /**
     * 消耗总时长
     */
    private Long consumedTime;

    @Getter
    public enum Status {
        NOT_STARTED(0, "未开始"),
        IN_PROGRESS(1, "进行中"),
        PAUSE(2, "暂停"),
        CANCEL_ONCE(3, "取消本次"),
        CANCELLED(4, "已取消"),
        COMPLETED(5, "已完成"),
        ;
        private final int value;
        private final String title;
        Status(int value, String title) {
            this.value = value;
            this.title = title;
        }
    }
}
