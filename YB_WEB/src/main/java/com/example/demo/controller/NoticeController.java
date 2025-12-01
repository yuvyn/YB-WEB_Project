package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NoticeController {

	@GetMapping("/news/notice")
    public String notice() {
        return "board/notice";
    }
    
    @GetMapping("/news/update")
    public String update() {
        return "board/update";
    }
    
    @GetMapping("/freeboard")
    public String freeboard() {
        return "board/freeboard";
    }
    
    @GetMapping("/guild")
    public String guild() {
        return "board/guild";
    }
    
    @GetMapping("/growth_guide")
    public String growth_guide() {
        return "board/growth_guide";
    }
    
    @GetMapping("/news/event")
    public String event() {
        return "board/event";
    }
}