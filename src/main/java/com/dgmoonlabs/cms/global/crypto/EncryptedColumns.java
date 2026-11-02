package com.dgmoonlabs.cms.global.crypto;

import jakarta.persistence.Convert;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EncryptedColumns {
    private final EntityManagerFactory entityManagerFactory;
    private final JdbcTemplate jdbcTemplate;

    private volatile List<EncryptedColumn> columns;

    public List<EncryptedColumn> all() {
        if (columns == null) {
            List<EncryptedColumn> found = new ArrayList<>();
            entityManagerFactory.unwrap(SessionFactoryImplementor.class).getMappingMetamodel().forEachEntityDescriptor(descriptor -> {
                if (!(descriptor instanceof AbstractEntityPersister persister)) {
                    return;
                }
                for (Field field : persister.getMappedClass().getDeclaredFields()) {
                    Convert convert = field.getAnnotation(Convert.class);
                    if (convert != null && convert.converter() == EncryptedStringConverter.class) {
                        found.add(new EncryptedColumn(persister.getTableName(),
                                persister.getIdentifierColumnNames()[0],
                                persister.getPropertyColumnNames(field.getName())[0]));
                    }
                }
            });
            columns = List.copyOf(found);
        }
        return columns;
    }

    public void widen() {
        if (!isPostgres()) {
            return;
        }
        for (EncryptedColumn column : all()) {
            Integer length = jdbcTemplate.query(
                    "select character_maximum_length from information_schema.columns "
                            + "where table_schema = current_schema() and table_name = ? and column_name = ?",
                    rs -> rs.next() ? (Integer) rs.getObject(1) : null,
                    unquote(column.table()), unquote(column.column()));
            if (length != null && length < EncryptedStringConverter.COLUMN_LENGTH) {
                jdbcTemplate.execute("alter table " + column.table() + " alter column " + column.column()
                        + " type varchar(" + EncryptedStringConverter.COLUMN_LENGTH + ")");
                log.info("암호화 컬럼 확장: {}.{} ({} -> {})", column.table(), column.column(), length,
                        EncryptedStringConverter.COLUMN_LENGTH);
            }
        }
    }

    public int reencrypt() {
        int changed = 0;
        for (EncryptedColumn column : all()) {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "select " + column.id() + " as row_id, " + column.column() + " as sealed from " + column.table()
                            + " where " + column.column() + " is not null");
            for (Map<String, Object> row : rows) {
                String stored = (String) row.get("sealed");
                if (DataKeys.isCurrent(stored)) {
                    continue;
                }
                jdbcTemplate.update("update " + column.table() + " set " + column.column() + " = ? where " + column.id() + " = ?",
                        DataKeys.protect(DataKeys.reveal(stored)), row.get("row_id"));
                changed++;
            }
        }
        return changed;
    }

    private boolean isPostgres() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            return "PostgreSQL".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName());
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String unquote(final String name) {
        return name.replace("\"", "").replace("`", "");
    }

    public record EncryptedColumn(String table, String id, String column) {
    }
}
