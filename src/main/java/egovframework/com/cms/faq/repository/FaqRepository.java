package egovframework.com.cms.faq.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import egovframework.com.cms.faq.dto.FaqSearch;
import egovframework.com.cms.faq.dto.FaqUpdateDto;
import egovframework.com.cms.faq.model.Faq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static egovframework.com.cms.faq.model.QFaq.faq;

@Repository
@RequiredArgsConstructor
public class FaqRepository {

    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;

    public void save(Faq faq) {
        entityManager.persist(faq);
    }

    public Optional<Faq> findById(Long id) {
        Faq faq = entityManager.find(Faq.class, id);

        return Optional.ofNullable(faq);
    }

    public List<Faq> findList(FaqSearch faqSearch) {
        if (faqSearch.isPaging())
            return pagingQuery(faqSearch);

        return nonPagingQuery(faqSearch);
    }

    public int count(FaqSearch faqSearch) {
        Long count = queryFactory
                .select(faq.count())
                .from(faq)
                .where(
                        questionLike(faqSearch.getQuestion()),
                        publishIs(faqSearch.getPublish()))
                .fetchOne();

        assert count != null;
        return count.intValue();
    }

    public void updateFaq(Faq faq, FaqUpdateDto faqUpdateDto) {
        faq.update(
                faqUpdateDto.getQuestion(),
                faqUpdateDto.getAnswer(),
                faqUpdateDto.getPublish());
    }

    public void delete(Faq faq) {
        entityManager.remove(faq);
    }

    private List<Faq> pagingQuery(FaqSearch faqSearch) {
        return queryFactory
                .selectFrom(faq)
                .orderBy(faq.id.desc())
                .where(
                        questionLike(faqSearch.getQuestion()),
                        publishIs(faqSearch.getPublish()))
                .offset(faqSearch.getOffset())
                .limit(faqSearch.getPageSize())
                .fetch();
    }

    private List<Faq> nonPagingQuery(FaqSearch faqSearch) {
        return queryFactory
                .selectFrom(faq)
                .orderBy(faq.id.desc())
                .where(
                        questionLike(faqSearch.getQuestion()),
                        publishIs(faqSearch.getPublish()))
                .fetch();
    }

    private Predicate questionLike(String questionParam) {
        return (questionParam != null) ? faq.question.contains(questionParam) : null;
    }

    private Predicate publishIs(Boolean publishParam) {
        return publishParam != null ? faq.publish.eq(publishParam) : null;
    }
}
