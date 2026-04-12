package com.sweetbook.mapper;

import com.sweetbook.vo.PetVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PetMapper {

    int insertPet(PetVO petVO);

    PetVO selectPetById(Long petId);

    List<PetVO> selectPetList();

    List<PetVO> selectPetListByMemberId(Long memberId);

    int updatePet(PetVO petVO);

    int deletePet(Long petId);
}