package cn.bincker.modules.auth.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {
    @Autowired
    private UserService userService;

    @Test
    void getUser2FAQRCodeUrl() {
        var user = userService.loadUserByUsername("bit16");
        if (user != null) {
            System.out.println(userService.getUser2FAQRCodeUrl(user));
        }
    }
}