package com.sweetbook.controller;

import com.sweetbook.service.BookProjectService;
import com.sweetbook.vo.BookProjectVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book-projects")
public class BookProjectController {

    private final BookProjectService bookProjectService;

    public BookProjectController(BookProjectService bookProjectService) {
        this.bookProjectService = bookProjectService;
    }

    @PostMapping
    public ResponseEntity<Long> createBookProject(@RequestBody BookProjectVO bookProjectVO) {
        Long bookProjectId = bookProjectService.createBookProject(bookProjectVO);
        return ResponseEntity.ok(bookProjectId);
    }

    @GetMapping("/{bookProjectId}")
    public ResponseEntity<BookProjectVO> getBookProject(@PathVariable("bookProjectId") Long bookProjectId) {
        BookProjectVO bookProjectVO = bookProjectService.getBookProjectById(bookProjectId);
        if (bookProjectVO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bookProjectVO);
    }

    @GetMapping
    public ResponseEntity<List<BookProjectVO>> getBookProjectList() {
        return ResponseEntity.ok(bookProjectService.getBookProjectList());
    }

    @GetMapping("/pet/{petId}")
    public ResponseEntity<List<BookProjectVO>> getBookProjectListByPetId(@PathVariable("petId") Long petId) {
        return ResponseEntity.ok(bookProjectService.getBookProjectListByPetId(petId));
    }

    @PutMapping("/{bookProjectId}")
    public ResponseEntity<String> modifyBookProject(@PathVariable("bookProjectId") Long bookProjectId,
                                                    @RequestBody BookProjectVO bookProjectVO) {
        bookProjectVO.setBookProjectId(bookProjectId);
        boolean result = bookProjectService.modifyBookProject(bookProjectVO);

        if (!result) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("책 프로젝트 수정 성공");
    }

    @DeleteMapping("/{bookProjectId}")
    public ResponseEntity<String> removeBookProject(@PathVariable("bookProjectId") Long bookProjectId) {
        boolean result = bookProjectService.removeBookProject(bookProjectId);

        if (!result) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("책 프로젝트 삭제 성공");
    }
}