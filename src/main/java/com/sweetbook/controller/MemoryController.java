package com.sweetbook.controller;

import com.sweetbook.service.MemoryService;
import com.sweetbook.vo.MemoryVO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/memories")
public class MemoryController {

    private final MemoryService memoryService;

    public MemoryController(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createMemory(
            @RequestParam("petId") Long petId,
            @RequestParam("content") String content,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        if (petId == null) {
            throw new IllegalArgumentException("petId가 없습니다.");
        }
        if (content == null || content.trim().isBlank()) {
            throw new IllegalArgumentException("코멘트를 입력해주세요.");
        }

        List<MemoryVO> existingMemories = memoryService.getMemoryListByPetId(petId);
        int nextOrder = existingMemories == null ? 1 : existingMemories.size() + 1;

        MemoryVO memoryVO = new MemoryVO();
        memoryVO.setPetId(petId);
        memoryVO.setChapterType("DAILY");
        memoryVO.setDisplayOrder(nextOrder);
        memoryVO.setTitle("추억" + nextOrder);
        memoryVO.setContent(content.trim());
        memoryVO.setIsRepresentative(nextOrder == 1 ? "Y" : "N");
        memoryVO.setImageUrl(saveMemoryImage(file));

        Long memoryId = memoryService.createMemory(memoryVO);
        return ResponseEntity.ok(memoryId);
    }

    @GetMapping("/{memoryId}")
    public ResponseEntity<MemoryVO> getMemory(@PathVariable("memoryId") Long memoryId) {
        MemoryVO memoryVO = memoryService.getMemoryById(memoryId);
        if (memoryVO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(memoryVO);
    }

    @GetMapping
    public ResponseEntity<List<MemoryVO>> getMemoryList() {
        return ResponseEntity.ok(memoryService.getMemoryList());
    }

    @GetMapping("/pet/{petId}")
    public ResponseEntity<List<MemoryVO>> getMemoryListByPetId(@PathVariable("petId") Long petId) {
        return ResponseEntity.ok(memoryService.getMemoryListByPetId(petId));
    }

    @PutMapping("/{memoryId}")
    public ResponseEntity<String> modifyMemory(@PathVariable("memoryId") Long memoryId,
                                               @RequestBody MemoryVO memoryVO) {
        memoryVO.setMemoryId(memoryId);
        boolean result = memoryService.modifyMemory(memoryVO);

        if (!result) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("추억 수정 성공");
    }

    @DeleteMapping("/{memoryId}")
    public ResponseEntity<String> removeMemory(@PathVariable("memoryId") Long memoryId) {
        boolean result = memoryService.removeMemory(memoryId);

        if (!result) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("추억 삭제 성공");
    }

    private String saveMemoryImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String uploadDirPath = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "memory";
            File uploadDir = new File(uploadDirPath);

            if (!uploadDir.exists() && !uploadDir.mkdirs()) {
                throw new IllegalStateException("추억 이미지 업로드 폴더를 생성하지 못했습니다.");
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

            return "/uploads/memory/" + savedFileName;

        } catch (Exception e) {
            throw new RuntimeException("추억 이미지 저장 실패: " + e.getMessage(), e);
        }
    }
}