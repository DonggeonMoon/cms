package com.dgmoonlabs.cms.domain.board.repository;


import com.dgmoonlabs.cms.domain.board.entity.aritcle.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
}
