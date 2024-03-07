package com.dgmoonlabs.cms.domain.board.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor(access = AccessLevel.NONE)
@AllArgsConstructor
public class BoardDto {
    private Long id;
    private String content;
}
