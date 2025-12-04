package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.demo.domain.EventPost;
import com.example.demo.service.EventPostService;

@Controller
@RequestMapping("/board/event")
public class EventController {

    private final EventPostService eventPostService;

    public EventController(EventPostService eventPostService) {
        this.eventPostService = eventPostService;
    }

    // 카드형 목록
    @GetMapping
    public String list(Model model) {
        List<EventPost> events = eventPostService.getList();
        model.addAttribute("events", events);
        return "board/event";  // 너가 만든 카드형 event.html
    }

    // 이벤트 작성 폼
    @GetMapping("/write")
    public String writeForm() {
        return "board/event_write";
    }

    // 이벤트 등록
    @PostMapping("/write")
    public String write(@RequestParam("title") String title,
                        @RequestParam("content") String content,
                        @RequestParam("thumbnailUrl") String thumbnailUrl,  // form name="thumbnailUrl"
                        @RequestParam("status") String status,              // form name="status"
                        @RequestParam("category") String category,          // form name="category"
                        @RequestParam("platform") String platform,          // form name="platform"
                        @RequestParam(name = "startDate", required = false) String startDate,
                        @RequestParam(name = "endDate", required = false) String endDate) {

        LocalDate start = (startDate == null || startDate.isBlank())
                ? null : LocalDate.parse(startDate);
        LocalDate end = (endDate == null || endDate.isBlank())
                ? null : LocalDate.parse(endDate);

        EventPost event = eventPostService.create(
                title, content, thumbnailUrl,
                status, category, platform,
                start, end
        );

        return "redirect:/event/" + event.getId();
    }

    // 상세 페이지
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        EventPost event = eventPostService.getEvent(id);
        model.addAttribute("event", event);
        return "board/event_detail";
    }
}