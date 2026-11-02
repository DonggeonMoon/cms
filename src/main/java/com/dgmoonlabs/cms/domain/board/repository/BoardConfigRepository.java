package com.dgmoonlabs.cms.domain.board.repository;

import com.dgmoonlabs.cms.domain.board.entity.aritcle.BoardConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardConfigRepository extends JpaRepository<BoardConfig, Long> {
    Optional<BoardConfig> findByBoardId(Long boardId);
}
