package com.sweetbook.controller;

import com.sweetbook.service.BookTransformService;
import com.sweetbook.vo.BookRequestPreviewVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookTransformController {

    private final BookTransformService bookTransformService;

    public BookTransformController(BookTransformService bookTransformService) {
        this.bookTransformService = bookTransformService;
    }

    @GetMapping("/projects/{petId}/preview")
    public ResponseEntity<BookRequestPreviewVO> previewBookRequest(
            @PathVariable("petId") Long petId,
            @RequestParam(value = "templateCode", required = false, defaultValue = "diary_a") String templateCode,
            @RequestParam(value = "bookSpecCode", required = false, defaultValue = "softcover_a5") String bookSpecCode) {

        BookRequestPreviewVO preview = bookTransformService.buildBookRequestPreview(petId, templateCode, bookSpecCode);
        return ResponseEntity.ok(preview);
    }
}