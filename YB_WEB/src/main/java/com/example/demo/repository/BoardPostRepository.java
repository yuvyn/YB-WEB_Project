package com.example.demo.repository;

import org.springframework.data.domain.Pageable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.BoardPost;
import com.example.demo.domain.BoardType;

public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {

	Page<BoardPost> findByBoardType(BoardType boardType, Pageable pageable);

    Page<BoardPost> findByBoardTypeAndTitleContainingIgnoreCase(
            BoardType boardType,
            String keyword,
            Pageable pageable
    );
	
    // 게시판별 목록
    List<BoardPost> findByBoardTypeOrderByNoticePinDescCreatedAtDesc(BoardType boardType);

    // 검색 (제목 기준)
    List<BoardPost> findByBoardTypeAndTitleContainingIgnoreCaseOrderByNoticePinDescCreatedAtDesc(
            BoardType boardType, String keyword
    );
    
    // 🔹 QNA + 카테고리 전체(키워드 없음)
    List<BoardPost> findByBoardTypeAndQnaCategoryOrderByNoticePinDescCreatedAtDesc(
            BoardType boardType, String qnaCategory);

    // 🔹 QNA + 카테고리 + 키워드
    List<BoardPost> findByBoardTypeAndQnaCategoryAndTitleContainingIgnoreCaseOrderByNoticePinDescCreatedAtDesc(
            BoardType boardType, String qnaCategory, String keyword);
    
 // 🔹 QNA + 카테고리 (페이징)
    Page<BoardPost> findByBoardTypeAndQnaCategory(
            BoardType boardType,
            String qnaCategory,
            Pageable pageable
    );

    // 🔹 QNA + 카테고리 + 키워드 (페이징)
    Page<BoardPost> findByBoardTypeAndQnaCategoryAndTitleContainingIgnoreCase(
            BoardType boardType,
            String qnaCategory,
            String keyword,
            Pageable pageable
    );
}
