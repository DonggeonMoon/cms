package com.dgmoonlabs.cms.domain.board.repository;

import com.dgmoonlabs.cms.domain.board.dto.BoardRequest;
import com.dgmoonlabs.cms.domain.board.entity.aritcle.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BoardCustomRepository {
    Page<Board> find(BoardRequest boardRequest, Pageable pageable);

    List<Board> find(BoardRequest boardRequest);
}
