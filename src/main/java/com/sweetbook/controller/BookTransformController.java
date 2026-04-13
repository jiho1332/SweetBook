package com.sweetbook.controller;

import com.sweetbook.service.BookTransformService;
import com.sweetbook.service.SweetBookApiService;
import com.sweetbook.vo.BookRequestPreviewVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookTransformController {

    private final BookTransformService bookTransformService;
    private final SweetBookApiService sweetBookApiService;

    public BookTransformController(BookTransformService bookTransformService,
                                   SweetBookApiService sweetBookApiService) {
        this.bookTransformService = bookTransformService;
        this.sweetBookApiService = sweetBookApiService;
    }

    // 🔥 템플릿 목록
    @GetMapping("/templates")
    public String getTemplates() {
        return sweetBookApiService.getTemplates();
    }

    // 🔥 판형 목록
    @GetMapping("/book-specs")
    public String getBookSpecs() {
        return sweetBookApiService.getBookSpecs();
    }

    // 🔥 외부 책 목록 (추가)
    @GetMapping("/list")
    public String getBooks() {
        return sweetBookApiService.getBooks();
    }

    // 🔥 preview
    @GetMapping("/projects/{petId}/preview")
    public ResponseEntity<BookRequestPreviewVO> previewBookRequest(
            @PathVariable("petId") Long petId,
            @RequestParam("templateCode") String templateCode,
            @RequestParam("bookSpecCode") String bookSpecCode) {

        BookRequestPreviewVO preview =
                bookTransformService.buildBookRequestPreview(petId, templateCode, bookSpecCode);

        return ResponseEntity.ok(preview);
    }

    // 🔥 create + contents + finalization까지 한 번에
    @PostMapping("/projects/{petId}/create")
    @ResponseBody
    public String createBook(@PathVariable("petId") Long petId,
                             @RequestParam("templateCode") String templateCode,
                             @RequestParam("bookSpecCode") String bookSpecCode) {

        BookRequestPreviewVO preview =
                bookTransformService.buildBookRequestPreview(petId, templateCode, bookSpecCode);

        // 1️⃣ 책 생성
        String bookUid = sweetBookApiService.createBook(preview);

        // 2️⃣ 콘텐츠 추가
        sweetBookApiService.addContents(bookUid, preview);

        // 3️⃣ 책 확정
        sweetBookApiService.finalizeBook(bookUid);

        return "🔥 책 생성 완료 (final까지): " + bookUid;
    }
}