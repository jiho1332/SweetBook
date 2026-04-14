package com.sweetbook.mapper;

import com.sweetbook.vo.BookProjectVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookProjectMapper {

    int insertBookProject(BookProjectVO bookProjectVO);

    BookProjectVO selectBookProjectById(Long bookProjectId);

    List<BookProjectVO> selectBookProjectList();

    List<BookProjectVO> selectBookProjectListByPetId(Long petId);

    int updateBookProject(BookProjectVO bookProjectVO);

    int deleteBookProject(Long bookProjectId);

    int updateBookProjectStatus(@Param("bookProjectId") Long bookProjectId,
                                @Param("status") String status);

    int updateBookCreated(@Param("bookProjectId") Long bookProjectId,
                          @Param("sweetbookBookId") String sweetbookBookId,
                          @Param("bookUid") String bookUid,
                          @Param("status") String status);

    int updateBookFinalized(@Param("bookProjectId") Long bookProjectId,
                            @Param("status") String status);

    int updateBookUid(@Param("bookProjectId") Long bookProjectId,
                      @Param("bookUid") String bookUid);

    int updateTemplateAndSpecUid(@Param("bookProjectId") Long bookProjectId,
                                 @Param("bookSpecUid") String bookSpecUid,
                                 @Param("coverTemplateUid") String coverTemplateUid,
                                 @Param("contentTemplateUid") String contentTemplateUid);

    BookProjectVO findById(Long id);
}