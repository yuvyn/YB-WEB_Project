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
}