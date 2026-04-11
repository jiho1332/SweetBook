package com.sweetbook.service;

import com.sweetbook.mapper.MemberMapper;
import com.sweetbook.vo.MemberVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;

    public MemberServiceImpl(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    @Override
    public Long createMember(MemberVO memberVO) {
        memberMapper.insertMember(memberVO);
        return memberVO.getMemberId();
    }

    @Override
    public MemberVO getMemberById(Long memberId) {
        return memberMapper.selectMemberById(memberId);
    }

    @Override
    public List<MemberVO> getMemberList() {
        return memberMapper.selectMemberList();
    }

    @Override
    public boolean modifyMember(MemberVO memberVO) {
        return memberMapper.updateMember(memberVO) > 0;
    }

    @Override
    public boolean removeMember(Long memberId) {
        return memberMapper.deleteMember(memberId) > 0;
    }
}