package com.sweetbook.service;

import com.sweetbook.vo.PetVO;

import java.util.List;

public interface PetService {

    Long createPet(PetVO petVO);

    PetVO getPetById(Long petId);

    List<PetVO> getPetList();

    List<PetVO> getPetListByMemberId(Long memberId);

    boolean modifyPet(PetVO petVO);

    boolean removePet(Long petId);
}