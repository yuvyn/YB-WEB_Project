package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.BoardPost;
import com.example.demo.domain.BoardType;
import com.example.demo.repository.BoardPostRepository;

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
                           boolean noticePin) {

        BoardPost post = new BoardPost(boardType, title, content, writer, memberId);
        post.setNoticePin(noticePin);
        return boardPostRepository.save(post);
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

    // 단건 조회 + 조회수 증가
    public BoardPost getPost(Long id) {
        BoardPost post = boardPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        post.increaseViewCount();
        return post;
    }
}