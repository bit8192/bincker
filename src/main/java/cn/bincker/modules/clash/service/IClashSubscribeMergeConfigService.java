package cn.bincker.modules.clash.service;

import cn.bincker.modules.clash.dto.ClashSubscribeMergeConfigDto;
import cn.bincker.modules.clash.entity.ClashSubscribeMergeConfig;
import cn.bincker.modules.clash.vo.ClashSubscribeMergeConfigDetailVo;
import cn.bincker.modules.clash.vo.ClashSubscribeMergeConfigVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface IClashSubscribeMergeConfigService {
    Page<ClashSubscribeMergeConfigVo> page(ClashSubscribeMergeConfigDto dto, Page<ClashSubscribeMergeConfig> page);
    ClashSubscribeMergeConfig getByToken(String token);
    ClashSubscribeMergeConfig add(ClashSubscribeMergeConfigDto dto);
    ClashSubscribeMergeConfig update(ClashSubscribeMergeConfigDto dto);
    void delete(Long id);
    ClashSubscribeMergeConfigDetailVo getDetailById(Long id);
}
