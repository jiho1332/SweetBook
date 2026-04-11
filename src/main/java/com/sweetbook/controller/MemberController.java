package com.sweetbook.controller;

import com.sweetbook.service.MemberService;
import com.sweetbook.vo.MemberVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<Long> createMember(@RequestBody MemberVO memberVO) {
        Long memberId = memberService.createMember(memberVO);
        return ResponseEntity.ok(memberId);
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberVO> getMember(@PathVariable("memberId") Long memberId) {
        MemberVO memberVO = memberService.getMemberById(memberId);
        if (memberVO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(memberVO);
    }
    @GetMapping
    public ResponseEntity<List<MemberVO>> getMemberList() {
        return ResponseEntity.ok(memberService.getMemberList());
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<String> updateMember(@PathVariable("memberId") Long memberId,
                                               @RequestBody MemberVO memberVO) {
        memberVO.setMemberId(memberId);
        boolean updated = memberService.modifyMember(memberVO);
        if (!updated) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("회원 수정 완료");
    }
    @DeleteMapping("/{memberId}")
    public ResponseEntity<String> deleteMember(@PathVariable("memberId") Long memberId) {
        boolean deleted = memberService.removeMember(memberId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("회원 삭제 완료");
    }
}