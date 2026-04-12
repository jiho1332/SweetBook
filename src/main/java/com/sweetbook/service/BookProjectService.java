package com.sweetbook.service;

import com.sweetbook.vo.BookProjectVO;

import java.util.List;

public interface BookProjectService {

    Long createBookProject(BookProjectVO bookProjectVO);

    BookProjectVO getBookProjectById(Long bookProjectId);

    List<BookProjectVO> getBookProjectList();

    List<BookProjectVO> getBookProjectListByPetId(Long petId);

    boolean modifyBookProject(BookProjectVO bookProjectVO);

    boolean removeBookProject(Long bookProjectId);

    boolean modifyBookProjectStatus(Long bookProjectId, String status);

    boolean modifyBookCreated(Long bookProjectId, String sweetbookBookId, String status);

    boolean modifyBookFinalized(Long bookProjectId, String status);
}