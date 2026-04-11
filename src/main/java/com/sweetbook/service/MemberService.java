package com.sweetbook.service;

import com.sweetbook.vo.MemberVO;

import java.util.List;

public interface MemberService {

    Long createMember(MemberVO memberVO);

    MemberVO getMemberById(Long memberId);

    List<MemberVO> getMemberList();

    boolean modifyMember(MemberVO memberVO);

    boolean removeMember(Long memberId);
}