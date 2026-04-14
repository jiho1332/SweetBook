package com.sweetbook.controller;

import com.sweetbook.service.BookProjectService;
import com.sweetbook.vo.BookProjectVO;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Map;
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
    public ResponseEntity<Long> createBookProject(@RequestBody Map<String, Object> request) {

        Long memberId = parseLong(request.get("memberId"), 1L);
        String petName = parseString(request.get("petName"));
        String profileImageUrl = parseString(request.get("profileImageUrl"));
        String memorialDate = parseString(request.get("memorialDate"));

        String title = parseString(request.get("title"));
        String coverTitle = parseString(request.get("coverTitle"));
        String coverSubtitle = parseString(request.get("coverSubtitle"));
        String dedicationText = parseString(request.get("dedicationText"));
        String templateCode = parseString(request.get("templateCode"));
        String bookSpecCode = parseString(request.get("bookSpecCode"));
        String status = parseString(request.get("status"));

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

            if (profileImageUrl.isBlank()) {
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(4, profileImageUrl);
            }

            if (memorialDate.isBlank()) {
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

    private Long parseLong(Object value, Long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        String str = value.toString().trim();
        if (str.isBlank()) {
            return defaultValue;
        }
        return Long.parseLong(str);
    }

    private String parseString(Object value) {
        return value == null ? "" : value.toString().trim();
    }
}