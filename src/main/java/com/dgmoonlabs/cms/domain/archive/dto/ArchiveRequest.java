package com.dgmoonlabs.cms.domain.archive.dto;

import lombok.*;

@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ArchiveRequest {
    private Long id;
}
