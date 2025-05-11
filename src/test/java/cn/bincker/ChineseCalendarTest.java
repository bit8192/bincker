package cn.bincker;

import cn.bincker.modules.todo.service.impl.TodoService;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;


public class ChineseCalendarTest {
    @Test
    void test() throws ParseException {
        var simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        var count = TodoService.calculateChineseTriggerCount(
                simpleDateFormat.parse("2024-03-11"),
                simpleDateFormat.parse("2023-05-10"),
                "1y"
        );
        System.out.println(count);
    }
}
