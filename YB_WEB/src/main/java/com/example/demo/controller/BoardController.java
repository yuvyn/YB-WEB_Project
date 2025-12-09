package com.example.demo.controller;

import com.example.demo.domain.BoardPost;
import com.example.demo.domain.BoardType;
import com.example.demo.domain.Member;
import com.example.demo.service.BoardPostService;

import jakarta.servlet.http.HttpSession;

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
                            Model model) {

        List<BoardPost> posts = boardPostService.getList(boardType, keyword);
        model.addAttribute("posts", posts);
        model.addAttribute("totalCount", posts.size());
        model.addAttribute("keyword", keyword);
        model.addAttribute("boardType", boardType);

        return viewName;
    }

    // ===== 목록들 =====

 // 공지사항
    @GetMapping("/notice")
    public String noticeList(@RequestParam(name = "keyword", required = false) String keyword,
                             Model model) {
        return listPage(BoardType.NOTICE, "board/notice", keyword, model);
    }

    // 업데이트
    @GetMapping("/update")
    public String updateList(@RequestParam(name = "keyword", required = false) String keyword,
                             Model model) {
        return listPage(BoardType.UPDATE, "board/update", keyword, model);
    }

    // 자유게시판
    @GetMapping("/free")
    public String freeList(@RequestParam(name = "keyword", required = false) String keyword,
                           Model model) {
        return listPage(BoardType.FREE, "board/freeboard", keyword, model);
    }

    // 문의게시판
    @GetMapping("/qna")
    public String qnaList(@RequestParam(name = "keyword", required = false) String keyword,
                          Model model) {
        return listPage(BoardType.QNA, "board/QnABoard", keyword, model);
    }
    
    // 가이드 게시판
    @GetMapping("/growth_guide")
    public String growth_guideList(@RequestParam(name = "keyword", required = false) String keyword,
                          Model model) {
        return listPage(BoardType.GROWTH_GUIDE, "board/growth_guide", keyword, model);
    }
    
    // 길드 게시판
    @GetMapping("/guild")
    public String guild(@RequestParam(name = "keyword", required = false) String keyword,
                          Model model) {
        return listPage(BoardType.guild, "board/guild", keyword, model);
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

    // 글쓰기 처리
    @PostMapping("/{type}/write")
    public String write(@PathVariable("type") String type,
                        @RequestParam("title") String title,
                        @RequestParam("content") String content,
                        @RequestParam(name = "noticePin", required = false, defaultValue = "false") boolean noticePin,
                        HttpSession session) {

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/member/login";
        }

        // ✅ 여기! getId() 말고 getIdx()
        Long memberId = loginMember.getIdx();
        String writer = loginMember.getNickname();

        BoardType boardType = BoardType.valueOf(type.toUpperCase());

        BoardPost post = boardPostService.write(
                boardType,
                title,
                content,
                writer,
                memberId,
                noticePin
        );

        return "redirect:/board/" + type.toLowerCase() + "/" + post.getId();
    }

    // 상세 페이지
    @GetMapping("/{type}/{id}")
    public String detail(@PathVariable("type") String type,
                         @PathVariable("id") Long id,
                         Model model) {
        BoardType boardType = BoardType.valueOf(type.toUpperCase());
        BoardPost post = boardPostService.getPost(id);

        model.addAttribute("post", post);
        model.addAttribute("boardType", boardType);

        return "board/detail";
    }
}