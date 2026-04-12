package com.sweetbook.service;

import com.sweetbook.mapper.MemoryMapper;
import com.sweetbook.vo.MemoryVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemoryServiceImpl implements MemoryService {

    private final MemoryMapper memoryMapper;

    public MemoryServiceImpl(MemoryMapper memoryMapper) {
        this.memoryMapper = memoryMapper;
    }

    @Override
    public Long createMemory(MemoryVO memoryVO) {
        memoryMapper.insertMemory(memoryVO);
        return memoryVO.getMemoryId();
    }

    @Override
    public MemoryVO getMemoryById(Long memoryId) {
        return memoryMapper.selectMemoryById(memoryId);
    }

    @Override
    public List<MemoryVO> getMemoryList() {
        return memoryMapper.selectMemoryList();
    }

    @Override
    public List<MemoryVO> getMemoryListByPetId(Long petId) {
        return memoryMapper.selectMemoryListByPetId(petId);
    }

    @Override
    public boolean modifyMemory(MemoryVO memoryVO) {
        return memoryMapper.updateMemory(memoryVO) > 0;
    }

    @Override
    public boolean removeMemory(Long memoryId) {
        return memoryMapper.deleteMemory(memoryId) > 0;
    }
}