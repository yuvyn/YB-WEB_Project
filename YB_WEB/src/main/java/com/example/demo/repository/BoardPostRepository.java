package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.BoardPost;
import com.example.demo.domain.BoardType;

public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {

    // 게시판별 목록
    List<BoardPost> findByBoardTypeOrderByNoticePinDescCreatedAtDesc(BoardType boardType);

    // 검색 (제목 기준)
    List<BoardPost> findByBoardTypeAndTitleContainingIgnoreCaseOrderByNoticePinDescCreatedAtDesc(
            BoardType boardType, String keyword
    );
}