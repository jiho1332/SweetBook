package com.sweetbook.mapper;

import com.sweetbook.vo.MemberVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MemberMapper {

    int insertMember(MemberVO memberVO);

    MemberVO selectMemberById(Long memberId);

    List<MemberVO> selectMemberList();

    int updateMember(MemberVO memberVO);

    int deleteMember(Long memberId);
}