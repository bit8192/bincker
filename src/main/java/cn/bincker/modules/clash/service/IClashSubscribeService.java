package cn.bincker.modules.clash.service;

import cn.bincker.modules.clash.dto.ClashSubscribeDto;
import cn.bincker.modules.clash.entity.ClashSubscribe;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface IClashSubscribeService {
    Page<ClashSubscribe> page(ClashSubscribeDto clashSubscribe, Page<ClashSubscribe> page);

    ClashSubscribe add(ClashSubscribeDto dto);

    ClashSubscribe update(ClashSubscribeDto dto);

    void delete(Long id);
}
