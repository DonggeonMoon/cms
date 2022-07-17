package egovframework.com.cms.config.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import egovframework.com.cms.config.dto.ConfigOptionSearch;
import egovframework.com.cms.config.dto.ConfigOptionUpdateDto;
import egovframework.com.cms.config.model.ConfigOption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static egovframework.com.cms.config.model.QConfigOption.configOption;


@Repository
@RequiredArgsConstructor
public class ConfigOptionRepository {
    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;

    public void save(ConfigOption configOption) {
        entityManager.persist(configOption);
    }

    public Optional<ConfigOption> findById(Long id) {
        ConfigOption config = entityManager.find(ConfigOption.class, id);

        return Optional.ofNullable(config);
    }

    public List<ConfigOption> findList(ConfigOptionSearch configSearch) {
        if (configSearch.isPaging())
            return pagingQuery(configSearch);

        return nonPagingQuery(configSearch);
    }

    public int count(ConfigOptionSearch configSearch) {
        Long count = queryFactory
                .select(configOption.count())
                .from(configOption)
                .where(
                        confIdIs(configSearch.getConfId()))
                .fetchOne();

        assert count != null;
        return count.intValue();
    }

    public void updateConfig(ConfigOption configOption, ConfigOptionUpdateDto configUpdateDto) {
        configOption.update(configUpdateDto.getConfId(), configUpdateDto.getOptKey(),
                configUpdateDto.getOptValue(), configUpdateDto.getOptName(), configUpdateDto.getOptHelp(),
                configUpdateDto.getOptType(), configUpdateDto.isOptHidden(), configUpdateDto.getOptUnitText());
    }

    public void delete(ConfigOption config) {
        entityManager.remove(config);
    }

    private List<ConfigOption> pagingQuery(ConfigOptionSearch configSearch) {
        return queryFactory
                .selectFrom(configOption)
                .orderBy(configOption.confId.desc())
                .where(
                        confIdIs(configSearch.getConfId()))
                .offset(configSearch.getOffset())
                .limit(configSearch.getPageSize())
                .fetch();
    }

    private List<ConfigOption> nonPagingQuery(ConfigOptionSearch configSearch) {
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
