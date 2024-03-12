package com.dgmoonlabs.cms.domain.board.mapper;

import com.dgmoonlabs.cms.domain.board.dto.ArticleDto;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BoardMapper {
    @Insert("insert board(content) values (#{board.content})")
    long insertBoard(@Param("board") ArticleDto articleDto);
}
