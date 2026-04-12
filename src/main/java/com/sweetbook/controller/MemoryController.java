package com.sweetbook.controller;

import com.sweetbook.service.MemoryService;
import com.sweetbook.vo.MemoryVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/memories")
public class MemoryController {

    private final MemoryService memoryService;

    public MemoryController(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    @PostMapping
    public ResponseEntity<Long> createMemory(@RequestBody MemoryVO memoryVO) {
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
}