package com.dgmoonlabs.cms.domain.board.service;

import com.dgmoonlabs.cms.domain.board.dto.BoardDto;
import com.dgmoonlabs.cms.domain.board.entity.Board;
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

    @Transactional(value = "transactionManager")
    public Long insertBoard() throws Exception {
            boardRepository.save(Board.builder().content("board1").build());
            throwException();
            boardMapper.insertBoard(BoardDto.builder().content("board2").build());
        return null;
    }

    private void throwException() throws Exception {
        throw new Exception("daf");
    }

}
