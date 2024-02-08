package com.nowcoder.community.controller;

import com.nowcoder.community.service.DateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.swing.plaf.PanelUI;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
public class DateController {
    @Autowired
    private DateService dateService;

    //统计页面
    @RequestMapping(path = "/data", method = {RequestMethod.GET, RequestMethod.POST})
    public String getDataPage() {
        return "/site/admin/data";
    }

    //统计UV
    @RequestMapping(path = "/data/uv", method = RequestMethod.POST)
    public String getUV(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start,
                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end, Model model) {
        long uv = dateService.calculateUV(start, end);
        model.addAttribute("uvResult", uv);
        model.addAttribute("uvStartDay", start);
        model.addAttribute("uvEndDay", end);
        return "forward:/data";
    }

    //统计DAU
    @RequestMapping(path = "/data/dau", method = RequestMethod.POST)
    public String getDAU(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start,
                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end, Model model) {
        long dau = dateService.calculateDAU(start, end);
        model.addAttribute("dauResult", dau);
        model.addAttribute("dauStartDay", start);
        model.addAttribute("dauEndDay", end);
        return "forward:/data";
    }
}
