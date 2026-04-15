package com.sweetbook.controller;

import com.sweetbook.service.BookProjectService;
import com.sweetbook.service.PetService;
import com.sweetbook.vo.BookProjectVO;
import com.sweetbook.vo.PetVO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
            @RequestParam(value = "contentTemplateUid", required = false) String contentTemplateUid,
            @RequestParam(value = "bookSpecCode", required = false) String bookSpecCode,
            @RequestParam(value = "bookSpecUid", required = false) String bookSpecUid,
            @RequestParam(value = "status", required = false, defaultValue = "DRAFT") String status,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        String finalBookSpecUid = firstNotBlank(bookSpecUid, bookSpecCode);
        String finalTemplateUid = firstNotBlank(contentTemplateUid, templateCode);

        if (petName == null || petName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("petName이 비어 있습니다.");
        }

        if (title == null || title.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("title이 비어 있습니다.");
        }

        if (finalBookSpecUid.isBlank()) {
            return ResponseEntity.badRequest().body("bookSpecUid가 비어 있습니다.");
        }

        if (finalTemplateUid.isBlank()) {
            return ResponseEntity.badRequest().body("contentTemplateUid가 비어 있습니다.");
        }

        try {
            Long savedId;

            if (bookProjectId == null) {
                savedId = createNewProject(
                        petName,
                        memorialDate,
                        title,
                        coverTitle,
                        coverSubtitle,
                        dedicationText,
                        finalBookSpecUid,
                        finalTemplateUid,
                        status,
                        file
                );
            } else {
                savedId = updateExistingProject(
                        bookProjectId,
                        petName,
                        memorialDate,
                        title,
                        coverTitle,
                        coverSubtitle,
                        dedicationText,
                        finalBookSpecUid,
                        finalTemplateUid,
                        status,
                        file
                );
            }

            return ResponseEntity.ok(String.valueOf(savedId));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("프로젝트 저장 실패: " + e.getMessage());
        }
    }

    @GetMapping("/{bookProjectId}")
    public ResponseEntity<BookProjectVO> getBookProject(@PathVariable("bookProjectId") Long bookProjectId) {
        BookProjectVO vo = bookProjectService.getBookProjectById(bookProjectId);
        if (vo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(vo);
    }

    @GetMapping
    public ResponseEntity<List<BookProjectVO>> getBookProjectList() {
        return ResponseEntity.ok(bookProjectService.getBookProjectList());
    }

    @DeleteMapping("/{bookProjectId}")
    public ResponseEntity<String> removeBookProject(@PathVariable("bookProjectId") Long bookProjectId) {
        boolean result = bookProjectService.removeBookProject(bookProjectId);

        if (!result) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("프로젝트 삭제 성공");
    }

    private Long createNewProject(String petName,
                                  String memorialDate,
                                  String title,
                                  String coverTitle,
                                  String coverSubtitle,
                                  String dedicationText,
                                  String bookSpecUid,
                                  String contentTemplateUid,
                                  String status,
                                  MultipartFile file) throws IOException {

        String profileImageUrl = null;
        if (file != null && !file.isEmpty()) {
            profileImageUrl = savePetImage(file);
        }

        PetVO pet = new PetVO();
        pet.setPetToken(UUID.randomUUID().toString());
        pet.setName(petName.trim());

        if (memorialDate != null && !memorialDate.isBlank()) {
            pet.setMemorialDate(LocalDate.parse(memorialDate));
        }

        if (profileImageUrl != null && !profileImageUrl.isBlank()) {
            pet.setProfileImageUrl(profileImageUrl);
        }

        Long petId = petService.createPet(pet);
        PetVO savedPet = petService.getPetById(petId);

        if (savedPet == null) {
            throw new IllegalStateException("pet 저장 후 조회 실패");
        }

        BookProjectVO vo = new BookProjectVO();
        vo.setPetId(savedPet.getPetId());
        vo.setTitle(title.trim());
        vo.setCoverTitle(firstNotBlank(coverTitle, title).trim());
        vo.setCoverSubtitle(coverSubtitle);
        vo.setDedicationText(dedicationText);
        vo.setStatus(status);
        vo.setBookSpecUid(bookSpecUid);
        vo.setContentTemplateUid(contentTemplateUid);

        // 기존 호환용
        vo.setBookSpecCode(bookSpecUid);
        vo.setTemplateCode(contentTemplateUid);

        return bookProjectService.createBookProject(vo);
    }

    private Long updateExistingProject(Long bookProjectId,
                                       String petName,
                                       String memorialDate,
                                       String title,
                                       String coverTitle,
                                       String coverSubtitle,
                                       String dedicationText,
                                       String bookSpecUid,
                                       String contentTemplateUid,
                                       String status,
                                       MultipartFile file) throws IOException {

        BookProjectVO existingProject = bookProjectService.getBookProjectById(bookProjectId);
        if (existingProject == null) {
            throw new IllegalArgumentException("수정할 bookProject가 존재하지 않습니다. bookProjectId=" + bookProjectId);
        }

        Long petId = existingProject.getPetId();
        if (petId == null) {
            throw new IllegalArgumentException("기존 project에 petId가 없습니다.");
        }

        PetVO existingPet = petService.getPetById(petId);
        if (existingPet == null) {
            throw new IllegalArgumentException("기존 pet이 존재하지 않습니다. petId=" + petId);
        }

        String profileImageUrl = existingPet.getProfileImageUrl();

        if (file != null && !file.isEmpty()) {
            profileImageUrl = savePetImage(file);
        }

        PetVO pet = new PetVO();
        pet.setPetId(existingPet.getPetId());
        pet.setPetToken(existingPet.getPetToken());
        pet.setName(petName.trim());

        if (memorialDate != null && !memorialDate.isBlank()) {
            pet.setMemorialDate(LocalDate.parse(memorialDate));
        } else {
            pet.setMemorialDate(null);
        }

        pet.setProfileImageUrl(profileImageUrl);

        boolean petUpdated = petService.modifyPet(pet);
        if (!petUpdated) {
            throw new IllegalStateException("pet 수정 실패");
        }

        BookProjectVO vo = new BookProjectVO();
        vo.setBookProjectId(bookProjectId);
        vo.setPetId(petId);
        vo.setTitle(title.trim());
        vo.setCoverTitle(firstNotBlank(coverTitle, title).trim());
        vo.setCoverSubtitle(coverSubtitle);
        vo.setDedicationText(dedicationText);
        vo.setStatus(status);

        // 기존 bookUid, sweetbookBookId 유지 필요
        vo.setBookUid(existingProject.getBookUid());
        vo.setSweetbookBookId(existingProject.getSweetbookBookId());

        vo.setBookSpecUid(bookSpecUid);
        vo.setContentTemplateUid(contentTemplateUid);

        // 기존 호환용
        vo.setBookSpecCode(bookSpecUid);
        vo.setTemplateCode(contentTemplateUid);

        boolean updated = bookProjectService.modifyBookProject(vo);
        if (!updated) {
            throw new IllegalStateException("bookProject 수정 실패");
        }

        return bookProjectId;
    }

    private String savePetImage(MultipartFile file) throws IOException {
        String uploadDirPath = System.getProperty("user.dir") + "/uploads/pet";
        File uploadDir = new File(uploadDirPath);

        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String savedFileName = UUID.randomUUID().toString().replace("-", "") + extension;
        File destination = new File(uploadDir, savedFileName);

        file.transferTo(destination);

        return "/uploads/pet/" + savedFileName;
    }

    private String firstNotBlank(String first, String second) {
        if (first != null && !first.trim().isEmpty()) {
            return first.trim();
        }
        if (second != null && !second.trim().isEmpty()) {
            return second.trim();
        }
        return "";
    }
}