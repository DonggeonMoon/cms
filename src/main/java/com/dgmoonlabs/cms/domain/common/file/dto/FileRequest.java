package com.dgmoonlabs.cms.domain.common.file.dto;

import lombok.*;

@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class FileRequest {
    private Long id;
}
