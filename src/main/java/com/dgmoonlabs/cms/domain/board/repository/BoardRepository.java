package com.dgmoonlabs.cms.domain.board.repository;

import com.dgmoonlabs.cms.domain.board.entity.aritcle.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    Optional<Board> findByIdAndSiteIdAndDeletedAtIsNull(Long id, Long siteId);

    List<Board> findAllBySiteIdAndDeletedAtIsNullOrderByIdAsc(Long siteId);

    boolean existsByNameAndSiteId(String name, Long siteId);

    boolean existsByNameAndSiteIdAndIdNot(String name, Long siteId, Long id);
}
