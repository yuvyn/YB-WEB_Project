package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String main(Model model) {
        model.addAttribute("title", "메인 페이지");
        return "Main/Main";
    }
    
    @GetMapping("/eventB")
    public String eventA() {
        return "event/eventB";
    }
    
    @GetMapping("/game")
    public String game() {
        return "game/game";
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