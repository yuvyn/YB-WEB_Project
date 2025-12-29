package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard() {
        // templates/admin/admin-dashboard.html
        return "admin/admin-dashboard";
    }

    @GetMapping("/access-log")
    public String accessLog() {
        // templates/admin/admin-access-log.html
        return "admin/admin-access-log";
    }
}