package com.dgmoonlabs.cms.domain.board.controller;

import com.dgmoonlabs.cms.domain.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    @GetMapping("/test")
    public String test() throws Exception {
        Long inserted = boardService.insertBoard();
        return  "completed" + inserted;
    }
}
