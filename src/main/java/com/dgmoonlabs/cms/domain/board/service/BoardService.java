package com.dgmoonlabs.cms.domain.board.service;

import com.dgmoonlabs.cms.domain.board.dto.ArticleDto;
import com.dgmoonlabs.cms.domain.board.mapper.BoardMapper;
import com.dgmoonlabs.cms.domain.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardMapper boardMapper;

    @Transactional
    public Long insertBoard() throws Exception {
            boardMapper.insertBoard(ArticleDto.builder().content("board2").build());
        return null;
    }

}
