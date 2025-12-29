package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {

    @GetMapping("/")
    public String main(Model model) {
        model.addAttribute("title", "메인 페이지");
        return "Main/Main";
    }
    
    @GetMapping("/eventA")
    public String eventA() {
        return "event/eventA";
    }
    
    @GetMapping("/eventB")
    public String eventB() {
        return "event/eventB";
    }
    
    @GetMapping("/QnA")
    public String QnA() {
        return "board/QnAboard";
    }
    
    @GetMapping("/signup")
    public String signup() {
        return "login/signup_select";
    }
}