package com.leyou.page.controller;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

    @Autowired
    private PageService pageService;

    @GetMapping("item/{spuId}.html")
    public String LoadData(@PathVariable("spuId")Long spuId, Model model){
        System.out.println(spuId);
        model.addAllAttributes(this.pageService.LoadData(spuId));
        return "item";
    }
}
