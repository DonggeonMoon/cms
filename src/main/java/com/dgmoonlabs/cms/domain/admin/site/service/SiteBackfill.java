package com.dgmoonlabs.cms.domain.admin.site.service;

import com.dgmoonlabs.cms.domain.admin.site.entity.Site;
import com.dgmoonlabs.cms.domain.admin.site.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** 멀티사이트 도입 전부터 있던 행에 사이트를 지정한다. 마이그레이션 도입 시 제거. */
@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public class SiteBackfill implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;
    private final SiteRepository siteRepository;
    private final SiteService siteService;

    @Override
    public void run(final ApplicationArguments args) {
        Long defaultSiteId = siteRepository.findFirstByIsDefaultTrueAndDeletedAtIsNullOrderByIdAsc().map(Site::getId).orElse(null);
        if (defaultSiteId != null) {
            siteService.claimOrphanedRows(defaultSiteId);
        }
        dropStaleBoardNameUniqueConstraint();
    }

    /** board.name 은 전체 유일이었다가 사이트별 유일로 바뀌었는데, ddl-auto=update 는 기존 제약을 지우지 않으므로 직접 지운다. */
    private void dropStaleBoardNameUniqueConstraint() {
        if (!isPostgres()) {
            return;
        }
        List<String> constraints = jdbcTemplate.query("""
                select con.conname
                from pg_constraint con
                join pg_class c on c.oid = con.conrelid
                join pg_namespace n on n.oid = c.relnamespace
                where con.contype = 'u' and n.nspname = current_schema() and c.relname = 'board'
                  and (select array_agg(a.attname::text order by a.attname) from pg_attribute a
                       where a.attrelid = con.conrelid and a.attnum = any(con.conkey)) = array['name']
                """, (rs, row) -> rs.getString(1));
        for (String constraint : constraints) {
            jdbcTemplate.execute("alter table board drop constraint \"" + constraint + "\"");
        }
        if (!constraints.isEmpty()) {
            log.info("Dropped {} stale board.name unique constraint(s)", constraints.size());
        }
    }

    private boolean isPostgres() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            return "PostgreSQL".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName());
        } catch (SQLException e) {
            return false;
        }
    }
}
