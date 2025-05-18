package cn.bincker.modules.clash.service.impl;

import cn.bincker.modules.clash.dto.ClashSubscribeDto;
import cn.bincker.modules.clash.entity.ClashSubscribe;
import cn.bincker.modules.clash.mapper.ClashSubscribeMapper;
import cn.bincker.modules.clash.service.IClashSubscribeService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class ClashSubscribeService implements IClashSubscribeService {
    private final ClashSubscribeMapper clashSubscribeMapper;

    public ClashSubscribeService(ClashSubscribeMapper clashSubscribeMapper) {
        this.clashSubscribeMapper = clashSubscribeMapper;
    }

    @Override
    public Page<ClashSubscribe> page(ClashSubscribeDto clashSubscribeDto, Page<ClashSubscribe> page) {
        // 这里只做简单的name和url模糊查询示例，可根据实际需求扩展
        return clashSubscribeMapper.selectPage(page, new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ClashSubscribe>()
                .like(clashSubscribeDto.getName() != null, "name", clashSubscribeDto.getName())
                .like(clashSubscribeDto.getUrl() != null, "url", clashSubscribeDto.getUrl())
                .orderByAsc("(select min(value) from json_each(ifnull(latest_delay, '{}')))")
        );
    }

    @Override
    public ClashSubscribe add(ClashSubscribeDto dto) {
        ClashSubscribe entity = new ClashSubscribe();
        BeanUtils.copyProperties(dto, entity);
        clashSubscribeMapper.insert(entity);
        return entity;
    }

    @Override
    public ClashSubscribe update(ClashSubscribeDto dto) {
        Assert.notNull(dto, "参数不能为空");
        Assert.notNull(dto.getName(), "名称不能为空");
        // 这里只以name为唯一标识，实际可根据id等主键
        ClashSubscribe entity = clashSubscribeMapper.selectById(dto.getId());
        if (entity == null) throw new IllegalArgumentException("未找到对应订阅");
        BeanUtils.copyProperties(dto, entity);
        clashSubscribeMapper.updateById(entity);
        return entity;
    }

    @Override
    public void delete(Long id) {
        clashSubscribeMapper.deleteById(id);
    }
} 