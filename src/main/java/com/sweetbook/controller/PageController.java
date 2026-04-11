package com.sweetbook.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/projects/view")
    public String projectListPage() {
        return "project_list";
    }
}