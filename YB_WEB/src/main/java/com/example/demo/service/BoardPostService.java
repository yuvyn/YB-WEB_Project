package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.BoardPost;
import com.example.demo.domain.BoardType;
import com.example.demo.repository.BoardPostRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
@Transactional
public class BoardPostService {

    private final BoardPostRepository boardPostRepository;

    public BoardPostService(BoardPostRepository boardPostRepository) {
        this.boardPostRepository = boardPostRepository;
    }

    // 글 작성
    public BoardPost write(BoardType boardType,
            String title,
            String content,
            String writer,
            Long memberId,
            boolean noticePin,
            String qnaCategory,
            boolean secret) {

			BoardPost post = new BoardPost(boardType, title, content, writer, memberId);
			post.setNoticePin(noticePin);
			post.setSecret(secret);
			
			if (boardType == BoardType.QNA) {
			post.setQnaCategory(qnaCategory);
			}
			
			return boardPostRepository.save(post);
			}

 // 🔹 QNA 전용 목록 (카테고리 + 검색)
    @Transactional(readOnly = true)
    public Page<BoardPost> getQnaList(String keyword,
                                      String category,
                                      int page,
                                      int size) {

        // page는 0부터 시작 (컨트롤러에서 1 → 0으로 바꿔서 넘김)
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("noticePin"),   // 공지 먼저
                        Sort.Order.desc("createdAt")    // 최신순
                )
        );

        boolean noKeyword   = (keyword == null || keyword.isBlank());
        boolean allCategory = (category == null || "ALL".equalsIgnoreCase(category));

        // category: ALL / ACCOUNT / PAY / BUG / SUGGEST / ETC
        if (allCategory && noKeyword) {
            // 전체 + 검색 없음
            return boardPostRepository.findByBoardType(BoardType.QNA, pageable);

        } else if (allCategory) {
            // 전체 + 검색어
            return boardPostRepository.findByBoardTypeAndTitleContainingIgnoreCase(
                    BoardType.QNA, keyword, pageable
            );

        } else if (noKeyword) {
            // 카테고리만
            return boardPostRepository.findByBoardTypeAndQnaCategory(
                    BoardType.QNA, category, pageable
            );

        } else {
            // 카테고리 + 검색어
            return boardPostRepository.findByBoardTypeAndQnaCategoryAndTitleContainingIgnoreCase(
                    BoardType.QNA, category, keyword, pageable
            );
        }
    }
    
 // 🔹 QNA 처리 상태 변경
    public void updateQnaStatus(Long id, String newStatus) {
        BoardPost post = boardPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // QNA 게시글만 상태 변경
        if (post.getBoardType() != BoardType.QNA) {
            throw new IllegalStateException("문의글(QNA)에만 처리 상태를 변경할 수 있습니다.");
        }

        // 허용 값만 세팅
        if (!"RECEIVED".equals(newStatus) &&
            !"IN_PROGRESS".equals(newStatus) &&
            !"DONE".equals(newStatus)) {
            throw new IllegalArgumentException("잘못된 처리 상태입니다: " + newStatus);
        }

        post.setQnaStatus(newStatus);
        // @Transactional 이라 메서드 끝날 때 자동으로 update 쿼리 나감
    }
    
    // 게시판별 목록 조회(검색 포함)
    @Transactional(readOnly = true)
    public List<BoardPost> getList(BoardType boardType, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return boardPostRepository
                    .findByBoardTypeOrderByNoticePinDescCreatedAtDesc(boardType);
        } else {
            return boardPostRepository
                    .findByBoardTypeAndTitleContainingIgnoreCaseOrderByNoticePinDescCreatedAtDesc(
                            boardType, keyword
                    );
        }
    }
    
 // 🔹 페이징 목록 조회
    @Transactional(readOnly = true)
    public Page<BoardPost> getList(BoardType boardType, String keyword,
                                   int page, int size) {

        // page는 0부터 시작 (컨트롤러에서 1 → 0 보정해서 줄 것)
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("noticePin"),   // 공지 먼저
                        Sort.Order.desc("createdAt")    // 최신순
                )
        );

        if (keyword == null || keyword.isBlank()) {
            return boardPostRepository.findByBoardType(boardType, pageable);
        } else {
            return boardPostRepository.findByBoardTypeAndTitleContainingIgnoreCase(
                    boardType, keyword, pageable
            );
        }
    }

    // 단건 조회 + 조회수 증가
    public BoardPost getPost(Long id) {
        BoardPost post = boardPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        post.increaseViewCount();
        return post;
    }
    
 // 🔹 게시글 수정
    public BoardPost updatePost(BoardType boardType,
            Long id,
            String title,
            String content,
            boolean noticePin,
            String qnaCategory,
            boolean secret) {

			BoardPost post = boardPostRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
			
			if (post.getBoardType() != boardType) {
			throw new IllegalStateException("게시판 유형이 일치하지 않습니다.");
			}
			
			post.setTitle(title);
			post.setContent(content);
			post.setNoticePin(noticePin);
			post.setSecret(secret);   // 🔹 비밀글 여부 반영
			
			if (boardType == BoardType.QNA) {
			post.setQnaCategory(qnaCategory);
			} else {
			post.setQnaCategory(null);
			}
			
			return post;
			}

    // 🔹 게시글 삭제
    public void deletePost(BoardType boardType, Long id) {
        BoardPost post = boardPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        if (post.getBoardType() != boardType) {
            throw new IllegalStateException("게시판 유형이 일치하지 않습니다.");
        }

        boardPostRepository.delete(post);
    }
}