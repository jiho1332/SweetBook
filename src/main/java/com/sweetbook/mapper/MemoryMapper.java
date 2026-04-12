package com.sweetbook.mapper;

import com.sweetbook.vo.MemoryVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MemoryMapper {

    int insertMemory(MemoryVO memoryVO);

    MemoryVO selectMemoryById(Long memoryId);

    List<MemoryVO> selectMemoryList();

    List<MemoryVO> selectMemoryListByPetId(Long petId);

    int updateMemory(MemoryVO memoryVO);

    int deleteMemory(Long memoryId);
}