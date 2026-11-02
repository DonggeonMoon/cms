package com.dgmoonlabs.cms.domain.board.dto;

import org.springframework.data.web.PagedModel;

import java.util.List;

public record ArticleListResponse(List<ArticleSummaryResponse> notices, PagedModel<ArticleSummaryResponse> articles) {
}
