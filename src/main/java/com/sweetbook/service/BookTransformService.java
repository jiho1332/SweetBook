package com.sweetbook.service;

import com.sweetbook.vo.BookRequestPreviewVO;

import java.util.Map;

public interface BookTransformService {

    BookRequestPreviewVO buildBookRequestPreview(Long petId, String templateCode, String bookSpecCode);

    Map<String, Object> applyTemplate(Long bookProjectId);
}