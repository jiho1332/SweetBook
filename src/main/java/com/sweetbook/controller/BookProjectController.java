package com.sweetbook.controller;

import com.sweetbook.service.BookProjectService;
import com.sweetbook.service.PetService;
import com.sweetbook.vo.BookProjectVO;
import com.sweetbook.vo.PetVO;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/book-projects")
public class BookProjectController {

    private final BookProjectService bookProjectService;
    private final PetService petService;

    public BookProjectController(BookProjectService bookProjectService,
                                 PetService petService) {
        this.bookProjectService = bookProjectService;
        this.petService = petService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> saveBookProject(
            @RequestParam(value = "bookProjectId", required = false) Long bookProjectId,
            @RequestParam("petName") String petName,
            @RequestParam(value = "memorialDate", required = false) String memorialDate,
            @RequestParam("title") String title,
            @RequestParam(value = "coverTitle", required = false) String coverTitle,
            @RequestParam(value = "coverSubtitle", required = false) String coverSubtitle,
            @RequestParam(value = "dedicationText", required = false) String dedicationText,
            @RequestParam(value = "templateCode", required = false) String templateCode,
            @RequestParam(value = "bookSpecCode", required = false) String bookSpecCode,
            @RequestParam(value = "status", required = false, defaultValue = "DRAFT") String status,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        if (petName == null || petName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("petName이 비어 있습니다.");
        }

        if (title == null || title.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("title이 비어 있습니다.");
        }

        if (bookSpecCode == null || bookSpecCode.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("book_spec_uid가 비어 있습니다.");
        }

        if (templateCode == null || templateCode.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("content_template_uid가 비어 있습니다.");
        }

        PetVO pet = new PetVO();
        pet.setName(petName.trim());
        
        pet.setPetToken(UUID.randomUUID().toString());

        Long petId = petService.createPet(pet);
        PetVO savedPet = petService.getPetById(petId);

        BookProjectVO vo = new BookProjectVO();
        vo.setBookProjectId(bookProjectId);
        vo.setPetId(savedPet.getPetId());
        vo.setTitle(title.trim());
        vo.setCoverTitle(
                coverTitle != null && !coverTitle.trim().isEmpty()
                        ? coverTitle.trim()
                        : title.trim()
        );
        vo.setCoverSubtitle(coverSubtitle);
        vo.setDedicationText(dedicationText);
        vo.setMemorialDate(memorialDate);
        vo.setStatus(status);

        // JSP 이름은 code지만 실제 값은 UID
        vo.setBookSpecUid(bookSpecCode);
        vo.setContentTemplateUid(templateCode);

        // 화면 호환용
        vo.setBookSpecCode(bookSpecCode);
        vo.setTemplateCode(templateCode);

        Long savedId;
        if (bookProjectId == null) {
            savedId = bookProjectService.createBookProject(vo);
        } else {
            boolean updated = bookProjectService.modifyBookProject(vo);
            if (!updated) {
                return ResponseEntity.badRequest().body("책 프로젝트 수정 실패");
            }
            savedId = bookProjectId;
        }

        return ResponseEntity.ok(String.valueOf(savedId));
    }

    @GetMapping("/{bookProjectId}")
    public ResponseEntity<BookProjectVO> getBookProject(@PathVariable("bookProjectId") Long bookProjectId) {
        BookProjectVO vo = bookProjectService.getBookProjectById(bookProjectId);
        if (vo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(vo);
    }
}