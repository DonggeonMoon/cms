package egovframework.com.cms.config.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import egovframework.com.cms.config.dto.Config;
import egovframework.com.cms.config.dto.ConfigSearch;
import egovframework.com.cms.config.dto.ConfigUpdateDto;
import egovframework.com.cms.config.model.ConfigOption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static egovframework.com.cms.config.model.QConfigOption.configOption;


@Repository
@RequiredArgsConstructor
public class ConfigRepository {
    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;

    public void save(ConfigOption configOption) {
        entityManager.persist(configOption);
    }

    public Optional<ConfigOption> findById(Long id) {
        ConfigOption config = entityManager.find(ConfigOption.class, id);

        return Optional.ofNullable(config);
    }

    public List<ConfigOption> findList(ConfigSearch configSearch) {
        if (configSearch.isPaging())
            return pagingQuery(configSearch);

        return nonPagingQuery(configSearch);
    }

    public int count(ConfigSearch configSearch) {
        Long count = queryFactory
                .select(configOption.count())
                .from(configOption)
                .where(
                        confIdIs(configSearch.getConfId()))
                .fetchOne();

        assert count != null;
        return count.intValue();
    }

    public void updateConfig(Config config, ConfigUpdateDto configUpdateDto) {

    }

    public void delete(Config config) {
        entityManager.remove(config);
    }

    private List<ConfigOption> pagingQuery(ConfigSearch configSearch) {
        return queryFactory
                .selectFrom(configOption)
                .orderBy(configOption.confId.desc())
                .where(
                        confIdIs(configSearch.getConfId()))
                .offset(configSearch.getOffset())
                .limit(configSearch.getPageSize())
                .fetch();
    }

    private List<ConfigOption> nonPagingQuery(ConfigSearch configSearch) {
        return queryFactory
                .selectFrom(configOption)
                .orderBy(configOption.confId.desc())
                .where(
                        confIdIs(configSearch.getConfId()))
                .fetch();
    }

    private Predicate confIdIs(String questionParam) {
        return (questionParam != null) ? configOption.confId.contains(questionParam) : null;
    }
}
