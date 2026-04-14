package com.sweetbook.controller;

import com.sweetbook.service.BookProjectService;
import com.sweetbook.vo.BookProjectVO;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/book-projects")
public class BookProjectController {

    private final BookProjectService bookProjectService;
    private final JdbcTemplate jdbcTemplate;

    public BookProjectController(BookProjectService bookProjectService,
                                 JdbcTemplate jdbcTemplate) {
        this.bookProjectService = bookProjectService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping
    public ResponseEntity<Long> createBookProject(
            @RequestParam("petName") String petName,
            @RequestParam(value = "memorialDate", required = false) String memorialDate,
            @RequestParam("title") String title,
            @RequestParam("coverTitle") String coverTitle,
            @RequestParam(value = "coverSubtitle", required = false) String coverSubtitle,
            @RequestParam(value = "dedicationText", required = false) String dedicationText,
            @RequestParam("templateCode") String templateCode,
            @RequestParam("bookSpecCode") String bookSpecCode,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        Long memberId = 1L;

        petName = safe(petName);
        memorialDate = safe(memorialDate);
        title = safe(title);
        coverTitle = safe(coverTitle);
        coverSubtitle = safe(coverSubtitle);
        dedicationText = safe(dedicationText);
        templateCode = safe(templateCode);
        bookSpecCode = safe(bookSpecCode);
        status = safe(status);

        if (petName.isBlank()) {
            throw new IllegalArgumentException("반려견 이름을 입력해주세요.");
        }
        if (title.isBlank()) {
            throw new IllegalArgumentException("책 제목을 입력해주세요.");
        }
        if (coverTitle.isBlank()) {
            throw new IllegalArgumentException("표지 제목을 입력해주세요.");
        }
        if (templateCode.isBlank()) {
            throw new IllegalArgumentException("템플릿을 선택해주세요.");
        }
        if (bookSpecCode.isBlank()) {
            throw new IllegalArgumentException("판형을 선택해주세요.");
        }
        if (status.isBlank()) {
            status = "DRAFT";
        }

        String profileImageUrl = saveProfileImage(file);
        Long petId = createPet(memberId, petName, profileImageUrl, memorialDate);

        BookProjectVO bookProjectVO = new BookProjectVO();
        bookProjectVO.setPetId(petId);
        bookProjectVO.setTitle(title);
        bookProjectVO.setCoverTitle(coverTitle);
        bookProjectVO.setCoverSubtitle(coverSubtitle);
        bookProjectVO.setDedicationText(dedicationText);
        bookProjectVO.setTemplateCode(templateCode);
        bookProjectVO.setBookSpecCode(bookSpecCode);
        bookProjectVO.setStatus(status);

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

    private Long createPet(Long memberId, String petName, String profileImageUrl, String memorialDate) {
        String petToken = "PET_" + UUID.randomUUID().toString().replace("-", "");

        String insertSql = """
                INSERT INTO pet (member_id, pet_token, name, profile_image_url, memorial_date)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, memberId);
            ps.setString(2, petToken);
            ps.setString(3, petName);

            if (profileImageUrl == null || profileImageUrl.isBlank()) {
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(4, profileImageUrl);
            }

            if (memorialDate == null || memorialDate.isBlank()) {
                ps.setNull(5, Types.DATE);
            } else {
                ps.setDate(5, java.sql.Date.valueOf(memorialDate));
            }

            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("반려견 저장 후 pet_id를 가져오지 못했습니다.");
        }

        return key.longValue();
    }

    private String saveProfileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String uploadDirPath = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "pet";
            File uploadDir = new File(uploadDirPath);

            if (!uploadDir.exists() && !uploadDir.mkdirs()) {
                throw new IllegalStateException("업로드 폴더를 생성하지 못했습니다.");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";

            if (originalFilename != null) {
                int dotIndex = originalFilename.lastIndexOf(".");
                if (dotIndex >= 0) {
                    extension = originalFilename.substring(dotIndex);
                }
            }

            String savedFileName = UUID.randomUUID().toString().replace("-", "") + extension;
            File dest = new File(uploadDir, savedFileName);
            file.transferTo(dest);

            return "/uploads/pet/" + savedFileName;

        } catch (Exception e) {
            throw new RuntimeException("대표 사진 저장 실패: " + e.getMessage(), e);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}