package cn.bincker.modules.clash.service;

import cn.bincker.modules.clash.dto.ClashSubscribeMergeConfigDto;
import cn.bincker.modules.clash.entity.ClashSubscribeMergeConfig;
import cn.bincker.modules.clash.vo.ClashSubscribeMergeConfigDetailVo;
import cn.bincker.modules.clash.vo.ClashSubscribeMergeConfigVo;
import cn.bincker.modules.clash.vo.ClashYamlContentVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface IClashSubscribeMergeConfigService {
    Page<ClashSubscribeMergeConfigVo> page(ClashSubscribeMergeConfigDto dto, Page<ClashSubscribeMergeConfig> page);

    ClashYamlContentVo getContent(String token);

    ClashSubscribeMergeConfig add(ClashSubscribeMergeConfigDto dto);

    ClashSubscribeMergeConfig update(ClashSubscribeMergeConfigDto dto);

    void delete(Long id);

    ClashSubscribeMergeConfigDetailVo getDetailById(Long id);

    String getTokenById(Long id);

    void refresh(Long id);
}
