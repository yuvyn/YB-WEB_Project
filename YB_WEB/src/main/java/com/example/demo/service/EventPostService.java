package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.EventPost;
import com.example.demo.repository.EventPostRepository;

@Service
@Transactional
public class EventPostService {

    private final EventPostRepository eventPostRepository;

    public EventPostService(EventPostRepository eventPostRepository) {
        this.eventPostRepository = eventPostRepository;
    }

    // 이벤트 등록
    public EventPost create(String title,
                            String content,
                            String thumbnailUrl,
                            String status,
                            String category,
                            String platform,
                            LocalDate startDate,
                            LocalDate endDate) {

        EventPost event = new EventPost(
                title,
                content,
                thumbnailUrl,
                status,
                category,
                platform,
                startDate,
                endDate
        );

        return eventPostRepository.save(event);
    }

    // 목록 (지금은 단순 전체)
    @Transactional(readOnly = true)
    public List<EventPost> getList() {
        return eventPostRepository.findAllByOrderByStartDateDesc();
    }

    // 상세 보기 + 조회수 증가
    public EventPost getEvent(Long id) {
        EventPost event = eventPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이벤트입니다."));
        event.increaseViewCount();
        return event;
    }
}