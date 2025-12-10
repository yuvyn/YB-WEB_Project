package com.example.demo.controller;

import com.example.demo.domain.BoardPost;
import com.example.demo.domain.BoardType;
import com.example.demo.domain.Member;
import com.example.demo.service.BoardPostService;

import jakarta.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequestMapping("/board")
public class BoardController {

    private final BoardPostService boardPostService;

    public BoardController(BoardPostService boardPostService) {
        this.boardPostService = boardPostService;
    }

    // ===== 공통 목록 메서드 =====
    private String listPage(BoardType boardType,
            String viewName,
            String keyword,
            int page,          // 1부터 들어옴
            Model model) {

			int pageSize = 15;                     // 페이지당 15개
			int pageIndex = (page <= 0) ? 0 : page - 1;
			
			Page<BoardPost> pageResult =
			boardPostService.getList(boardType, keyword, pageIndex, pageSize);
			
			int totalPages = pageResult.getTotalPages();
			if (totalPages == 0) totalPages = 1;
			
			int currentPage = pageIndex + 1;
			
			// ===== 블록 페이징 계산 (5개씩) =====
			int blockSize = 5;
			int startPage = ((currentPage - 1) / blockSize) * blockSize + 1;
			int endPage = Math.min(startPage + blockSize - 1, totalPages);
			
			Integer prevBlockPage = (startPage > 1) ? startPage - 1 : null;
			Integer nextBlockPage = (endPage < totalPages) ? endPage + 1 : null;
			
			model.addAttribute("posts", pageResult.getContent());
			model.addAttribute("totalCount", pageResult.getTotalElements());
			model.addAttribute("boardType", boardType);
			model.addAttribute("keyword", keyword);
			
			// 페이징 정보
			model.addAttribute("page", currentPage);
			model.addAttribute("totalPages", totalPages);
			model.addAttribute("startPage", startPage);
			model.addAttribute("endPage", endPage);
			model.addAttribute("hasPrevBlock", prevBlockPage != null);
			model.addAttribute("hasNextBlock", nextBlockPage != null);
			model.addAttribute("prevBlockPage", prevBlockPage);
			model.addAttribute("nextBlockPage", nextBlockPage);
			
			return viewName;
			}

    // ===== 목록들 =====

 // 공지사항
    @GetMapping("/notice")
    public String noticeList(@RequestParam(name = "keyword", required = false) String keyword,
    						 @RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        return listPage(BoardType.NOTICE, "board/notice", keyword, page, model);
    }

    // 업데이트
    @GetMapping("/update")
    public String updateList(@RequestParam(name = "keyword", required = false) String keyword,
    						 @RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        return listPage(BoardType.UPDATE, "board/update", keyword, page, model);
    }

    // 자유게시판
    @GetMapping("/free")
    public String freeList(@RequestParam(name = "keyword", required = false) String keyword,
    					   @RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        return listPage(BoardType.FREE, "board/freeboard", keyword, page, model);
    }

    // 문의게시판
    @GetMapping("/qna")
    public String qnaList(@RequestParam(name = "keyword", required = false) String keyword,
                          @RequestParam(name = "category", required = false, defaultValue = "ALL") String category,
                          @RequestParam(name = "page", defaultValue = "1") int page,   // 🔹 페이지 번호(1부터)
                          Model model) {

        int pageSize = 15;                           // 페이지당 15개
        int pageIndex = (page <= 0) ? 0 : page - 1;  // JPA는 0부터

        Page<BoardPost> pageResult =
                boardPostService.getQnaList(keyword, category, pageIndex, pageSize);

        int totalPages = pageResult.getTotalPages();
        if (totalPages == 0) totalPages = 1;

        int currentPage = pageIndex + 1;

        // ===== 블록 페이징 (5개씩) =====
        int blockSize = 5;
        int startPage = ((currentPage - 1) / blockSize) * blockSize + 1;
        int endPage   = Math.min(startPage + blockSize - 1, totalPages);

        Integer prevBlockPage = (startPage > 1) ? startPage - 1 : null;
        Integer nextBlockPage = (endPage < totalPages) ? endPage + 1 : null;

        // 목록 + 검색/카테고리 정보
        model.addAttribute("posts", pageResult.getContent());
        model.addAttribute("totalCount", pageResult.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("boardType", BoardType.QNA);
        model.addAttribute("category", category);

        // 페이징 정보
        model.addAttribute("page", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("hasPrevBlock", prevBlockPage != null);
        model.addAttribute("hasNextBlock", nextBlockPage != null);
        model.addAttribute("prevBlockPage", prevBlockPage);
        model.addAttribute("nextBlockPage", nextBlockPage);

        return "board/QnABoard";
    }
    
    // 🔹 QNA 처리 상태 변경 (관리자 전용)
    @PostMapping("/qna/{id}/status")
    public String updateQnaStatus(@PathVariable("id") Long id,
                                  @RequestParam("status") String status,
                                  HttpSession session) {

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/member/login";
        }

        // 여기서 role 체크 (예: "ADMIN")
        // 실제 필드명이 role인지 memberRole인지에 따라 맞춰줘야 해
        if (!"ADMIN".equalsIgnoreCase(loginMember.getRole())) {
            // 권한 없으면 그냥 상세 페이지로 돌려보내기
            return "redirect:/board/qna/" + id;
        }

        boardPostService.updateQnaStatus(id, status);

        return "redirect:/board/qna/" + id;
    }
    
    // 가이드 게시판
    @GetMapping("/growth_guide")
    public String growth_guideList(@RequestParam(name = "keyword", required = false) String keyword,
    							   @RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        return listPage(BoardType.GROWTH_GUIDE, "board/growth_guide", keyword, page, model);
    }
    
    // 길드 게시판
    @GetMapping("/guild")
    public String guild(@RequestParam(name = "keyword", required = false) String keyword,
    					@RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        return listPage(BoardType.guild, "board/guild", keyword, page, model);
    }

    // ===== 글쓰기 / 상세 =====

    // 글쓰기 폼
    @GetMapping("/{type}/write")
    public String writeForm(@PathVariable("type") String type,
                            HttpSession session,
                            Model model) {

        BoardType boardType = BoardType.valueOf(type.toUpperCase());

        // 로그인 회원 가져오기
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            // 로그인 안 돼 있으면 로그인 페이지로
            return "redirect:/member/login";
        }

        model.addAttribute("boardType", boardType);
        model.addAttribute("loginMember", loginMember); // ★ 템플릿에서 쓸 수 있게 추가

        return "board/write";
    }
    
    // 🔹 수정 폼
    @GetMapping("/{type}/{id}/edit")
    public String editForm(@PathVariable("type") String type,
                           @PathVariable("id") Long id,
                           HttpSession session,
                           Model model) {

        BoardType boardType = BoardType.valueOf(type.toUpperCase());
        Member loginMember = (Member) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        BoardPost post = boardPostService.getPost(id);

        // 권한 체크: 작성자 또는 ADMIN만
        boolean isOwner = post.getMemberId() != null
                && post.getMemberId().equals(loginMember.getIdx());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(loginMember.getRole());

        if (!isOwner && !isAdmin) {
            // 권한 없으면 상세 페이지로 돌려보내기
            return "redirect:/board/" + type.toLowerCase() + "/" + id;
        }

        model.addAttribute("boardType", boardType);
        model.addAttribute("post", post);
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("isEdit", true);   // 수정 모드

        // 글쓰기 폼 재사용
        return "board/write";
    }

    // 글쓰기 처리
    @PostMapping("/{type}/write")
    public String write(@PathVariable("type") String type,
                        @RequestParam("title") String title,
                        @RequestParam("content") String content,
                        @RequestParam(name = "noticePin", required = false, defaultValue = "false") boolean noticePin,
                        @RequestParam(name = "qnaCategory", required = false) String qnaCategory,
                        HttpSession session) {

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/member/login";
        }

        Long memberId = loginMember.getIdx();
        String writer = loginMember.getNickname();

        BoardType boardType = BoardType.valueOf(type.toUpperCase());

        // 🔹 QNA가 아닌 게시판은 카테고리 null로
        if (boardType != BoardType.QNA) {
            qnaCategory = null;
        }

        BoardPost post = boardPostService.write(
                boardType,
                title,
                content,
                writer,
                memberId,
                noticePin,
                qnaCategory
        );

        return "redirect:/board/" + type.toLowerCase() + "/" + post.getId();
    }
    
    // 🔹 수정 처리
    @PostMapping("/{type}/{id}/edit")
    public String edit(@PathVariable("type") String type,
                       @PathVariable("id") Long id,
                       @RequestParam("title") String title,
                       @RequestParam("content") String content,
                       @RequestParam(name = "noticePin", required = false, defaultValue = "false") boolean noticePin,
                       @RequestParam(name = "qnaCategory", required = false) String qnaCategory,
                       HttpSession session) {

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/member/login";
        }

        BoardType boardType = BoardType.valueOf(type.toUpperCase());

        BoardPost post = boardPostService.getPost(id);

        boolean isOwner = post.getMemberId() != null
                && post.getMemberId().equals(loginMember.getIdx());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(loginMember.getRole());

        if (!isOwner && !isAdmin) {
            return "redirect:/board/" + type.toLowerCase() + "/" + id;
        }

        if (boardType != BoardType.QNA) {
            qnaCategory = null;
        }

        boardPostService.updatePost(boardType, id, title, content, noticePin, qnaCategory);

        return "redirect:/board/" + type.toLowerCase() + "/" + id;
    }
    
    // 🔹 삭제 처리
    @PostMapping("/{type}/{id}/delete")
    public String delete(@PathVariable("type") String type,
                         @PathVariable("id") Long id,
                         HttpSession session) {

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/member/login";
        }

        BoardType boardType = BoardType.valueOf(type.toUpperCase());
        BoardPost post = boardPostService.getPost(id);

        boolean isOwner = post.getMemberId() != null
                && post.getMemberId().equals(loginMember.getIdx());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(loginMember.getRole());

        if (!isOwner && !isAdmin) {
            return "redirect:/board/" + type.toLowerCase() + "/" + id;
        }

        boardPostService.deletePost(boardType, id);

        // 삭제 후 목록으로
        return "redirect:/board/" + type.toLowerCase();
    }

    // 상세 페이지
    @GetMapping("/{type}/{id}")
    public String detail(@PathVariable("type") String type,
                         @PathVariable("id") Long id,
                         HttpSession session,
                         Model model) {

        BoardType boardType = BoardType.valueOf(type.toUpperCase());
        BoardPost post = boardPostService.getPost(id);

        Member loginMember = (Member) session.getAttribute("loginMember");

        model.addAttribute("post", post);
        model.addAttribute("boardType", boardType);
        model.addAttribute("loginMember", loginMember);

        return "board/detail";
    }
}