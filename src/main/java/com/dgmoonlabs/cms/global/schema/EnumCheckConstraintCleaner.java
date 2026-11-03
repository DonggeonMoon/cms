package com.dgmoonlabs.cms.global.schema;

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

// ddl-auto=update 는 enum CHECK 제약을 고치지 않아 새 enum 값 저장이 실패하므로 지운다. 마이그레이션 도입 시 제거.
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class EnumCheckConstraintCleaner implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(final ApplicationArguments args) {
        if (!isPostgres()) {
            return;
        }
        List<String[]> constraints = jdbcTemplate.query("""
                        select c.relname, con.conname
                        from pg_constraint con
                        join pg_class c on c.oid = con.conrelid
                        join pg_namespace n on n.oid = c.relnamespace
                        where con.contype = 'c' and n.nspname = current_schema()
                          and pg_get_constraintdef(con.oid) like '%= ANY (ARRAY[%'
                        """,
                (rs, row) -> new String[]{rs.getString(1), rs.getString(2)});
        for (String[] constraint : constraints) {
            jdbcTemplate.execute("alter table \"%s\" drop constraint \"%s\"".formatted(constraint[0], constraint[1]));
        }
        if (!constraints.isEmpty()) {
            log.info("Dropped {} enum check constraints", constraints.size());
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
