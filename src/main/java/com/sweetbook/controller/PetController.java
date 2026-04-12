package com.sweetbook.controller;

import com.sweetbook.service.PetService;
import com.sweetbook.vo.PetVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @PostMapping
    public ResponseEntity<Long> createPet(@RequestBody PetVO petVO) {
        Long petId = petService.createPet(petVO);
        return ResponseEntity.ok(petId);
    }

    @GetMapping("/{petId}")
    public ResponseEntity<PetVO> getPet(@PathVariable("petId") Long petId) {
        PetVO petVO = petService.getPetById(petId);
        if (petVO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(petVO);
    }

    @GetMapping
    public ResponseEntity<List<PetVO>> getPetList() {
        return ResponseEntity.ok(petService.getPetList());
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<PetVO>> getPetListByMemberId(@PathVariable("memberId") Long memberId) {
        return ResponseEntity.ok(petService.getPetListByMemberId(memberId));
    }

    @PutMapping("/{petId}")
    public ResponseEntity<String> modifyPet(@PathVariable("petId") Long petId,
                                            @RequestBody PetVO petVO) {
        petVO.setPetId(petId);
        boolean result = petService.modifyPet(petVO);

        if (!result) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("반려견 정보 수정 성공");
    }

    @DeleteMapping("/{petId}")
    public ResponseEntity<String> removePet(@PathVariable("petId") Long petId) {
        boolean result = petService.removePet(petId);

        if (!result) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("반려견 정보 삭제 성공");
    }
}