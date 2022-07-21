package egovframework.com.cms.site.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import egovframework.com.cms.site.dto.SiteSearch;
import egovframework.com.cms.site.dto.SiteUpdateDto;
import egovframework.com.cms.site.model.Site;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static egovframework.com.cms.site.model.QSite.site;

@Repository
@RequiredArgsConstructor
public class SiteRepository {
    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;

    public void save(Site site) {
        entityManager.persist(site);
    }

    public Optional<Site> findById(String id) {
        Site site = entityManager.find(Site.class, id);

        return Optional.ofNullable(site);
    }

    public List<Site> findList(SiteSearch siteSearch) {
        if (siteSearch.isPaging())
            return pagingQuery(siteSearch);

        return nonPagingQuery(siteSearch);
    }

    public int count(SiteSearch siteSearch) {
        Long count = queryFactory
                .select(site.count())
                .from(site)
                .fetchOne();

        assert count != null;
        return count.intValue();
    }

    public void updateConfig(Site site, SiteUpdateDto siteUpdateDto) {
    }

    public void delete(Site site) {
        entityManager.remove(site);
    }

    private List<Site> pagingQuery(SiteSearch siteSearch) {
        return queryFactory
                .selectFrom(site)
                .orderBy(site.siteId.desc())
                .offset(siteSearch.getOffset())
                .limit(siteSearch.getPageSize())
                .fetch();
    }

    private List<Site> nonPagingQuery(SiteSearch siteSearch) {
        return queryFactory
                .selectFrom(site)
                .orderBy(site.siteId.desc())
                .fetch();
    }
}
