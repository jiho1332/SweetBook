package com.sweetbook.controller;

import com.sweetbook.service.BookProjectService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sweetbook.service.BookTransformService;
import com.sweetbook.service.SweetBookApiService;
import com.sweetbook.vo.BookProjectVO;
import com.sweetbook.vo.BookRequestPreviewVO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
public class BookTransformController {

    private final BookProjectService bookProjectService;
    private final BookTransformService bookTransformService;
    private final SweetBookApiService sweetBookApiService;

    public BookTransformController(BookProjectService bookProjectService,
                                   BookTransformService bookTransformService,
                                   SweetBookApiService sweetBookApiService) {
        this.bookProjectService = bookProjectService;
        this.bookTransformService = bookTransformService;
        this.sweetBookApiService = sweetBookApiService;
    }

    @GetMapping(value = "/templates", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTemplates() {
        return ResponseEntity.ok(sweetBookApiService.getTemplates());
    }

    @GetMapping(value = "/templates/by-spec", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, String>>> getTemplatesByBookSpecUid(
            @RequestParam("bookSpecUid") String bookSpecUid
    ) {
        return ResponseEntity.ok(sweetBookApiService.getTemplatesByBookSpecUid(bookSpecUid));
    }

    @GetMapping(value = "/book-specs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getBookSpecs() {
        return ResponseEntity.ok(sweetBookApiService.getBookSpecs());
    }

    @GetMapping(value = "/book-specs/normalized", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, String>>> getNormalizedBookSpecs() {
        return ResponseEntity.ok(sweetBookApiService.getNormalizedBookSpecs());
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getBooks() {
        return ResponseEntity.ok(sweetBookApiService.getBooks());
    }

    @GetMapping("/book-projects/{bookProjectId}/preview")
    public ResponseEntity<BookRequestPreviewVO> previewBookRequest(
            @PathVariable("bookProjectId") Long bookProjectId
    ) {
        BookProjectVO project = bookProjectService.getBookProjectById(bookProjectId);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }

        BookRequestPreviewVO preview = bookTransformService.buildBookRequestPreview(
                project.getPetId(),
                project.getContentTemplateUid() != null ? project.getContentTemplateUid() : project.getTemplateCode(),
                project.getBookSpecUid() != null ? project.getBookSpecUid() : project.getBookSpecCode()
        );

        return ResponseEntity.ok(preview);
    }

    @PostMapping("/book-projects/{bookProjectId}/apply-template")
    public ResponseEntity<Map<String, Object>> applyTemplate(
            @PathVariable("bookProjectId") Long bookProjectId
    ) {
        return ResponseEntity.ok(bookTransformService.applyTemplate(bookProjectId));
    }
    @PostMapping("/book-projects/{bookProjectId}/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(
            @PathVariable("bookProjectId") Long bookProjectId
    ) {
        BookProjectVO project = bookProjectService.getBookProjectById(bookProjectId);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }

        if (project.getBookUid() == null || project.getBookUid().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "bookUid가 없습니다. 먼저 책 완성을 진행해주세요."
            ));
        }

        try {
            sweetBookApiService.chargeSandboxCredit(100000);

            String orderResponse = sweetBookApiService.createOrder(project.getBookUid());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(orderResponse);
            JsonNode data = root.path("data");

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", root.path("success").asBoolean());
            result.put("message", root.path("message").asText());
            result.put("orderUid", data.path("orderUid").asText());
            result.put("orderStatus", data.path("orderStatus").asInt());
            result.put("orderStatusDisplay", data.path("orderStatusDisplay").asText());
            result.put("totalAmount", data.path("totalAmount").asDouble());
            result.put("bookUid", project.getBookUid());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}