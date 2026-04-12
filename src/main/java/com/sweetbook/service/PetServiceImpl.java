package com.sweetbook.service;

import com.sweetbook.mapper.PetMapper;
import com.sweetbook.vo.PetVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PetServiceImpl implements PetService {

    private final PetMapper petMapper;

    public PetServiceImpl(PetMapper petMapper) {
        this.petMapper = petMapper;
    }

    @Override
    public Long createPet(PetVO petVO) {
        petMapper.insertPet(petVO);
        return petVO.getPetId();
    }

    @Override
    public PetVO getPetById(Long petId) {
        return petMapper.selectPetById(petId);
    }

    @Override
    public List<PetVO> getPetList() {
        return petMapper.selectPetList();
    }

    @Override
    public List<PetVO> getPetListByMemberId(Long memberId) {
        return petMapper.selectPetListByMemberId(memberId);
    }

    @Override
    public boolean modifyPet(PetVO petVO) {
        return petMapper.updatePet(petVO) > 0;
    }

    @Override
    public boolean removePet(Long petId) {
        return petMapper.deletePet(petId) > 0;
    }
}