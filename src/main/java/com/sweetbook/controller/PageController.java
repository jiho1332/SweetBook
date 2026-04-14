package com.sweetbook.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/test/book")
    public String bookTestPage() {
        return "book_test";
    }
    @GetMapping("/memory-add")
    public String memoryAddPage() {
        return "memory_add";
    }
    @GetMapping("/review")
    public String reviewPage() {
        return "review";
    }
}