package com.sweetbook.service;

import com.sweetbook.vo.BookRequestPreviewVO;

public interface BookTransformService {

    BookRequestPreviewVO buildBookRequestPreview(Long petId, String templateCode, String bookSpecCode);
}