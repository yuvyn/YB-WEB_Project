package com.example.demo.repository;

import com.example.demo.domain.BoardComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {

	// ✅ 새로: 시간순으로만 가져오기
	List<BoardComment> findByPostIdOrderByCreatedAtDesc(Long postId);
	
	 // 🔹 내가 쓴 댓글 최근 20개
    List<BoardComment> findTop20ByMemberIdOrderByCreatedAtDesc(Long memberId);
    
    // 🔹 자식 댓글 존재 여부 확인
    boolean existsByParentId(Long parentId);
    
    Page<BoardComment> findByMemberId(Long memberId, Pageable pageable);
}