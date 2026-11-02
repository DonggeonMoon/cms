package com.dgmoonlabs.cms.domain.board.repository;

import com.dgmoonlabs.cms.domain.board.entity.aritcle.BoardArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface BoardArticleCustomRepository {
    Page<BoardArticle> search(Long boardId, String keyword, boolean includeHidden, LocalDateTime now, Pageable pageable);

    List<BoardArticle> findActiveNotices(Long boardId, boolean includeHidden, LocalDateTime now);
}
