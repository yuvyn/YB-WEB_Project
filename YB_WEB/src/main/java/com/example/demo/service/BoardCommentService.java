package com.example.demo.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.BoardComment;
import com.example.demo.domain.BoardPost;
import com.example.demo.repository.BoardCommentRepository;
import com.example.demo.repository.BoardPostRepository;

@Service
@Transactional
public class BoardCommentService {

    private final BoardCommentRepository boardCommentRepository;
    private final BoardPostRepository boardPostRepository;

    public BoardCommentService(BoardCommentRepository boardCommentRepository,
                               BoardPostRepository boardPostRepository) {
        this.boardCommentRepository = boardCommentRepository;
        this.boardPostRepository = boardPostRepository;
    }

    // 🔹 댓글/답글 목록 (부모 밑에 자식 정렬)
    @Transactional(readOnly = true)
    public List<BoardComment> getComments(Long postId) {

        // 1) 댓글 전체를 최신순으로 가져오기
        List<BoardComment> all = boardCommentRepository
                .findByPostIdOrderByCreatedAtDesc(postId);

        // 2) parentId 기준으로 답글들을 묶기
        Map<Long, List<BoardComment>> childrenMap = all.stream()
                .filter(c -> c.getParent() != null)
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        // 3) 부모 댓글 + 자식 정렬 결과 리스트
        List<BoardComment> ordered = new ArrayList<>();

        // 4) 최신순 부모 댓글 출력
        for (BoardComment c : all) {
            // 부모 댓글만 pick
            if (c.getParent() == null) {
                ordered.add(c);

                // 부모 댓글의 답글은 오래된 순(ASC)으로 출력
                List<BoardComment> children = childrenMap.get(c.getId());
                if (children != null) {
                    children.sort(Comparator.comparing(BoardComment::getCreatedAt));
                    ordered.addAll(children);
                }
            }
        }

        return ordered;
    }

    private void addChildrenRecursive(List<BoardComment> ordered,
                                      Map<Long, List<BoardComment>> childrenMap,
                                      Long parentId) {

        List<BoardComment> children = childrenMap.get(parentId);
        if (children == null) return;

        // 이미 생성시간 asc로 정렬된 상태라 그냥 순서대로 사용
        for (BoardComment child : children) {
            ordered.add(child);
            // 답글의 답글까지 만들고 싶으면 이 줄 유지
            addChildrenRecursive(ordered, childrenMap, child.getId());
        }
    }

    // 댓글/답글 작성
    public BoardComment addComment(Long postId,
                                   Long memberId,
                                   String writer,
                                   String content,
                                   Long parentId,
                                   boolean secret) {

        BoardPost post = boardPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        BoardComment parent = null;
        if (parentId != null) {
            parent = boardCommentRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 부모 댓글입니다."));
            if (!parent.getPost().getId().equals(postId)) {
                throw new IllegalStateException("부모 댓글이 해당 게시글의 댓글이 아닙니다.");
            }
        }

        BoardComment comment = new BoardComment(post, memberId, writer, content, parent, secret);
        return boardCommentRepository.save(comment);
    }

    // 댓글 수정
    public BoardComment updateComment(Long commentId, Long loginMemberId, boolean isAdmin, String content, boolean secret) {
        BoardComment comment = boardCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        if (!isAdmin && (comment.getMemberId() == null || !comment.getMemberId().equals(loginMemberId))) {
            throw new IllegalStateException("댓글 수정 권한이 없습니다.");
        }

        comment.setContent(content);
        comment.setSecret(secret);
        return comment;
    }

    // 댓글 삭제
    public void deleteComment(Long commentId, Long loginMemberId, boolean isAdmin) {
        BoardComment comment = boardCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        // 권한 체크
        if (!isAdmin && (comment.getMemberId() == null || !comment.getMemberId().equals(loginMemberId))) {
            throw new IllegalStateException("댓글 삭제 권한이 없습니다.");
        }

        boolean hasChildren = boardCommentRepository.existsByParentId(commentId);

        // 🔹 1) 자식이 있는 댓글(주로 부모 댓글): 소프트 삭제
        if (hasChildren) {
            comment.setDeleted(true);
            // 내용은 보여주지 않을 거라 굳이 지우진 않아도 되지만,
            // 깔끔하게 하고 싶으면 한 줄 넣어도 됨
            // comment.setContent("");
            return;
        }

        // 🔹 2) 자식이 없는 댓글: 실제 삭제
        BoardComment parent = comment.getParent();
        boardCommentRepository.delete(comment);

        // 🔹 3) 내가 답글이었고, 부모가 이미 삭제된 상태이며
        //        더 이상 다른 자식이 없으면 부모도 같이 삭제
        if (parent != null && parent.isDeleted()) {
            boolean parentHasOtherChildren = boardCommentRepository.existsByParentId(parent.getId());
            if (!parentHasOtherChildren) {
                boardCommentRepository.delete(parent);
            }
        }
    }
}