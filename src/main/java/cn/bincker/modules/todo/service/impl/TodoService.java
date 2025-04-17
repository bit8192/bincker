package cn.bincker.modules.todo.service.impl;

import cn.bincker.modules.todo.entity.Todo;
import cn.bincker.modules.todo.entity.TodoStatisticVo;
import cn.bincker.modules.todo.entity.UpdateTodoStatusDto;
import cn.bincker.modules.todo.mapper.TodoMapper;
import cn.bincker.modules.todo.service.ITodoService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.nlf.calendar.Lunar;
import com.nlf.calendar.Solar;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

@Service
public class TodoService implements ITodoService {
    private final TodoMapper todoMapper;

    public TodoService(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    @Override
    public TodoStatisticVo statistic() {
        var cyclicTodoList = todoMapper.selectList(Wrappers.<Todo>lambdaQuery().eq(Todo::getCyclic, Boolean.TRUE));
        var result = new TodoStatisticVo();
        for (Todo todo : cyclicTodoList) {
            //如果是农历则只统计以年为周期的数据
            var total = 0;
            if (todo.getIsChineseCalendar() != null){
                if (todo.getIsChineseCalendar() && todo.getCycle().matches("\\d+([yY])")){
                    //统计总数
                    total = calculateChineseTriggerCount(todo.getCreatedTime(), todo.getStartTime(), todo.getCycle());
                }
            }else {
                total = calculateTriggerCount(todo.getCreatedTime(), todo.getStartTime(), todo.getCycle());
            }
            var completed = todo.getCompletedCount();
            var cancelled = todo.getCancelledCount();
            var procrastination = todo.getProcrastinationCount();
            result.setTotal(result.getTotal() + total);
            result.setCompleted(result.getCompleted() + completed);
            result.setCancelled(result.getCancelled() + cancelled);
            result.setProcrastination(result.getProcrastination() + procrastination);
            result.setMiss(result.getMiss() + (total - completed - cancelled - procrastination));
        }
        return result;
    }

    /**
     * 计算农历经过了几次事件
     * @param startTime 开始计算时间（公历）
     * @param calculationPoint 计算时间点（公历）
     * @param periodExpression 周期表达式N年（1y\2y\3y...）
     */
    public static int calculateChineseTriggerCount(Date startTime,
                                                   Date calculationPoint,
                                                   String periodExpression){
        // 验证输入
        if (startTime == null || calculationPoint == null || periodExpression == null) {
            throw new IllegalArgumentException("参数不能为null");
        }
        var currentTime = new Date();
        if (startTime.getTime() > currentTime.getTime()) {
            return 0;
        }
        var matcher = Pattern.compile("(\\d+)[yY]").matcher(periodExpression);
        if (!matcher.find()) return 0;
        var period = Integer.parseInt(matcher.group(1));
        var startDate = new Solar(startTime);
        var pointLunar = new Lunar(calculationPoint);
        var count = solarIsAfterOrEquals(startDate, pointLunar.getSolar()) ? -1 : 0;
        var year = startDate.getLunar().getYear();
        var month = pointLunar.getMonth();
        var day = pointLunar.getDay();
        var calLunar = startDate.getLunar();
        var currLunar = new Lunar();
        while (calLunar.getSolar().isBefore(currLunar.getSolar())) {
            if (solarIsAfterOrEquals(calLunar.getSolar(), pointLunar.getSolar())) count++;
            year += period;
            calLunar = new Lunar(year, month, day);
            if (solarIsAfterOrEquals(calLunar.getSolar(), currLunar.getSolar())){
                if (solarIsAfterOrEquals(currLunar.getSolar(), pointLunar.getSolar())) count++;
            }
        }
        return count;
    }

    private static boolean solarIsAfterOrEquals(Solar solar1, Solar solar2) {
        if (solar1 == null || solar2 == null) return false;
        if (solar1.getYear() > solar2.getYear()) {
            return true;
        }
        if (solar1.getYear() < solar2.getYear()) {
            return false;
        }
        if (solar1.getMonth() > solar2.getMonth()) {
            return true;
        }
        if (solar1.getMonth() < solar2.getMonth()) {
            return false;
        }
        if (solar1.getDay() > solar2.getDay()) {
            return true;
        }
        if (solar1.getDay() < solar2.getDay()) {
            return false;
        }
        if (solar1.getHour() > solar2.getHour()) {
            return true;
        }
        if (solar1.getHour() < solar2.getHour()) {
            return false;
        }
        if (solar1.getMinute() > solar2.getMinute()) {
            return true;
        }
        if (solar1.getMinute() < solar2.getMinute()) {
            return false;
        }
        return solar1.getSecond() >= solar2.getSecond();
    }

    public static int calculateTriggerCount(Date startTime,
                                            Date calculationPoint,
                                            String periodExpression) {
        // 验证输入
        if (startTime == null || calculationPoint == null || periodExpression == null) {
            throw new IllegalArgumentException("参数不能为null");
        }

        var currentTime = new Date();
        if (startTime.getTime() > currentTime.getTime()) {
            return 0;
        }

        if (calculationPoint.getTime() < startTime.getTime()) {
            throw new IllegalArgumentException("计算时间点不能早于开始时间");
        }

        // 解析周期表达式
        var matcher = Pattern.compile("^(\\d+)([hdmy])$").matcher(periodExpression);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("无效的周期表达式格式: " + periodExpression);
        }

        int amount = Integer.parseInt(matcher.group(1));
        String unit = matcher.group(2);

        // 计算触发次数
        int count = startTime.getTime() > calculationPoint.getTime() ? -1 : 0;
        var nextTrigger = startTime;

        while (nextTrigger.getTime() < currentTime.getTime()) {
            if (nextTrigger.getTime() >= calculationPoint.getTime()) {
                count++;
            }
            var addType = switch (unit) {
                case "h" -> Calendar.HOUR;
                case "d" -> Calendar.DAY_OF_MONTH;
                case "m" -> Calendar.MONTH;
                case "y" -> Calendar.YEAR;
                default -> throw new IllegalStateException("Unexpected value: " + unit);
            };
            var calendar = Calendar.getInstance();
            calendar.setTime(nextTrigger);
            calendar.add(addType, amount);
            nextTrigger = calendar.getTime();
        }
        return count;
    }

    @Override
    public Page<Todo> getPage(PageDTO<Todo> dto) {
        return null;
    }

    @Override
    public Todo addTodo(Todo todo) {
        return null;
    }

    @Override
    public Todo updateStatus(UpdateTodoStatusDto dto) {
        return null;
    }
}
