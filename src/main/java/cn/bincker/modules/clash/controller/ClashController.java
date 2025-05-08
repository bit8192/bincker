package cn.bincker.modules.clash.controller;

import cn.bincker.modules.clash.dto.ClashSubscribeDto;
import cn.bincker.modules.clash.entity.ClashSubscribe;
import cn.bincker.modules.clash.service.IClashSubscribeMergeConfigService;
import cn.bincker.modules.clash.service.IClashSubscribeService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("clash")
public class ClashController {
    private final IClashSubscribeService clashSubscribeService;
    private final IClashSubscribeMergeConfigService clashSubscribeMergeConfigService;

    public ClashController(IClashSubscribeService clashSubscribeService, IClashSubscribeMergeConfigService clashSubscribeMergeConfigService) {
        this.clashSubscribeService = clashSubscribeService;
        this.clashSubscribeMergeConfigService = clashSubscribeMergeConfigService;
    }

    @GetMapping("subscribe")
    public String index(Model model, ClashSubscribeDto dto, Page<ClashSubscribe> page) {
        model.addAttribute("page", clashSubscribeService.page(dto, page));
        return "clash/subscribe";
    }

    @PostMapping("subscribe")
    public String addSubscribe(@Validated ClashSubscribeDto dto) {
        clashSubscribeService.add(dto);
        return "redirect:/clash/subscribe";
    }

    @PutMapping("subscribe")
    public String updateSubscribe(ClashSubscribeDto dto) {
        clashSubscribeService.update(dto);
        return "redirect:/clash/subscribe";
    }
}
