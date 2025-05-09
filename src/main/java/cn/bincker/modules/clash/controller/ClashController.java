package cn.bincker.modules.clash.controller;

import cn.bincker.modules.clash.dto.ClashSubscribeDto;
import cn.bincker.modules.clash.dto.ClashSubscribeMergeConfigDto;
import cn.bincker.modules.clash.entity.ClashSubscribe;
import cn.bincker.modules.clash.entity.ClashSubscribeMergeConfig;
import cn.bincker.modules.clash.service.IClashSubscribeMergeConfigService;
import cn.bincker.modules.clash.service.IClashSubscribeService;
import cn.bincker.modules.clash.vo.ClashSubscribeMergeConfigDetailVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("clash")
public class ClashController {
    private final IClashSubscribeService clashSubscribeService;
    private final IClashSubscribeMergeConfigService clashSubscribeMergeConfigService;

    public ClashController(IClashSubscribeService clashSubscribeService, IClashSubscribeMergeConfigService clashSubscribeMergeConfigService) {
        this.clashSubscribeService = clashSubscribeService;
        this.clashSubscribeMergeConfigService = clashSubscribeMergeConfigService;
    }

    @GetMapping(value = "subscribe", produces = {MediaType.TEXT_HTML_VALUE})
    public String subscribePage(Model model, ClashSubscribeDto dto, Page<ClashSubscribe> page) {
        model.addAttribute("page", clashSubscribeService.page(dto, page));
        return "clash/subscribe";
    }

    @GetMapping(value = "subscribe", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Page<ClashSubscribe>> subscribePageData(ClashSubscribeDto dto, Page<ClashSubscribe> page) {
        return ResponseEntity.ok(clashSubscribeService.page(dto, page));
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

    @DeleteMapping("subscribe/{id}")
    public void deleteSubscribe(@PathVariable Long id) {
        clashSubscribeService.delete(id);
    }

    @GetMapping("merge")
    public String mergePage(Model model, ClashSubscribeMergeConfigDto dto, Page<ClashSubscribeMergeConfig> page) {
        model.addAttribute("page", clashSubscribeMergeConfigService.page(dto, page));
        return "clash/merge";
    }

    @GetMapping(value = "merge/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClashSubscribeMergeConfigDetailVo> mergeDetail(@PathVariable Long id) {
        var detail = clashSubscribeMergeConfigService.getDetailById(id);
        if (detail == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(detail);
    }

    @PostMapping("merge")
    public String addMerge(@Validated ClashSubscribeMergeConfigDto dto) {
        clashSubscribeMergeConfigService.add(dto);
        return "redirect:/clash/merge";
    }

    @PutMapping("merge")
    public String updateMerge(@Validated ClashSubscribeMergeConfigDto dto) {
        clashSubscribeMergeConfigService.update(dto);
        return "redirect:/clash/merge";
    }

    @DeleteMapping("merge/{id}")
    public void deleteMerge(@PathVariable Long id) {
        clashSubscribeMergeConfigService.delete(id);
    }

    @GetMapping("config.yaml")
    public ResponseEntity<String> downloadConfig(@RequestParam String token) {
        var merge = clashSubscribeMergeConfigService.getByToken(token);
        if (merge == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(merge.getLatestMergeContent());
    }
}
