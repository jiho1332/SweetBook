package com.sweetbook.service;

import com.sweetbook.vo.MemoryVO;

import java.util.List;

public interface MemoryService {

    Long createMemory(MemoryVO memoryVO);

    MemoryVO getMemoryById(Long memoryId);

    List<MemoryVO> getMemoryList();

    List<MemoryVO> getMemoryListByPetId(Long petId);

    boolean modifyMemory(MemoryVO memoryVO);

    boolean removeMemory(Long memoryId);
}