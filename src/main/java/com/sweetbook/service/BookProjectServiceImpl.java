package com.sweetbook.service;

import com.sweetbook.constant.BookProjectStatus;
import com.sweetbook.mapper.BookProjectMapper;
import com.sweetbook.vo.BookProjectVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookProjectServiceImpl implements BookProjectService {

    private final BookProjectMapper bookProjectMapper;

    public BookProjectServiceImpl(BookProjectMapper bookProjectMapper) {
        this.bookProjectMapper = bookProjectMapper;
    }

    @Override
    public Long createBookProject(BookProjectVO bookProjectVO) {
        if (bookProjectVO.getStatus() == null || bookProjectVO.getStatus().isBlank()) {
            bookProjectVO.setStatus(BookProjectStatus.DRAFT);
        }
        bookProjectMapper.insertBookProject(bookProjectVO);
        return bookProjectVO.getBookProjectId();
    }

    @Override
    public BookProjectVO getBookProjectById(Long bookProjectId) {
        return bookProjectMapper.selectBookProjectById(bookProjectId);
    }

    @Override
    public List<BookProjectVO> getBookProjectList() {
        return bookProjectMapper.selectBookProjectList();
    }

    @Override
    public List<BookProjectVO> getBookProjectListByPetId(Long petId) {
        return bookProjectMapper.selectBookProjectListByPetId(petId);
    }

    @Override
    public boolean modifyBookProject(BookProjectVO bookProjectVO) {
        return bookProjectMapper.updateBookProject(bookProjectVO) > 0;
    }

    @Override
    public boolean removeBookProject(Long bookProjectId) {
        return bookProjectMapper.deleteBookProject(bookProjectId) > 0;
    }

    @Override
    public boolean modifyBookProjectStatus(Long bookProjectId, String status) {
        return bookProjectMapper.updateBookProjectStatus(bookProjectId, status) > 0;
    }

    @Override
    public boolean modifyBookCreated(Long bookProjectId, String sweetbookBookId, String bookUid, String status) {
        return bookProjectMapper.updateBookCreated(bookProjectId, sweetbookBookId, bookUid, status) > 0;
    }

    @Override
    public boolean modifyBookUid(Long bookProjectId, String bookUid) {
        return bookProjectMapper.updateBookUid(bookProjectId, bookUid) > 0;
    }

    @Override
    public boolean modifyTemplateAndSpecUid(Long bookProjectId,
                                            String bookSpecUid,
                                            String coverTemplateUid,
                                            String contentTemplateUid) {
        return bookProjectMapper.updateTemplateAndSpecUid(
                bookProjectId,
                bookSpecUid,
                coverTemplateUid,
                contentTemplateUid
        ) > 0;
    }

    @Override
    public boolean modifyBookFinalized(Long bookProjectId, String status) {
        return bookProjectMapper.updateBookFinalized(bookProjectId, status) > 0;
    }
}