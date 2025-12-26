package com.example.demo.supportchat.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminSupportChatPageController {

    @GetMapping("/admin/support")
    public String adminSupportPage() {
        return "admin/support_chat";
    }
}