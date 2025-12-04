package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.EventPost;

public interface EventPostRepository extends JpaRepository<EventPost, Long> {

    // 상태(진행/예정/종료)별 정렬
    List<EventPost> findAllByOrderByStartDateDesc();

    // 추후 필요하면 status/category 로 추가 검색 메서드 생성 가능
}