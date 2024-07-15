package com.dgmoonlabs.cms.domain.board.dto;

import com.dgmoonlabs.cms.domain.board.constant.BoardType;
import lombok.*;

@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class BoardRequest {
    private Long id;
    private String name;
    private BoardType type = BoardType.NORMAL;
    private String fields;
}
