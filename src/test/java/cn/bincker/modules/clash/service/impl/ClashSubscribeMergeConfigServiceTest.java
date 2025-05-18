package cn.bincker.modules.clash.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ClashSubscribeMergeConfigServiceTest {
    @Autowired
    private ClashSubscribeMergeConfigService clashSubscribeMergeConfigService;

    @Test
    void installMihomo() {
        clashSubscribeMergeConfigService.installMihomo();
    }
}