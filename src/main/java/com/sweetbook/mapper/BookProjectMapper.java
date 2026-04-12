package com.sweetbook.mapper;

import com.sweetbook.vo.BookProjectVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookProjectMapper {

    int insertBookProject(BookProjectVO bookProjectVO);

    BookProjectVO selectBookProjectById(Long bookProjectId);

    List<BookProjectVO> selectBookProjectListByPetId(Long petId);

    int updateBookProject(BookProjectVO bookProjectVO);

    int deleteBookProject(Long bookProjectId);

    int updateBookProjectStatus(@Param("bookProjectId") Long bookProjectId,
                                @Param("status") String status);

    int updateBookCreated(@Param("bookProjectId") Long bookProjectId,
                          @Param("sweetbookBookId") String sweetbookBookId,
                          @Param("status") String status);

    int updateBookFinalized(@Param("bookProjectId") Long bookProjectId,
                            @Param("status") String status);
}