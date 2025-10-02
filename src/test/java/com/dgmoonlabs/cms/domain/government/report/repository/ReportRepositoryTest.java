package com.dgmoonlabs.cms.domain.government.report.repository;

import com.dgmoonlabs.cms.domain.government.report.entity.Report;
import com.dgmoonlabs.cms.global.config.JpaConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class ReportRepositoryTest {
    @Autowired
    ReportRepository reportRepository;

    @Test
    void dbTest() {
        // CREATE
        Report saved = reportRepository.save(
                Report.builder()
                        .title("테스트 제목")
                        .contentText("샘플 텍스트 내용")
                        .contentHtml("<p>샘플 HTML 내용</p>")
                        .fields("{\"extra\":\"field\"}")
                        .userId(1001L)
                        .email("test@example.com")
                        .phoneNumber("010-1234-5678")
                        .build()
        );

        assertThat(saved.getId()).isNotNull();

        // READ
        Report found = reportRepository.findById(saved.getId())
                .orElseThrow();
        assertThat(found.getTitle()).isEqualTo("테스트 제목");

        // UPDATE
        found.update(
                "수정된 제목",
                "수정된 텍스트",
                "<p>수정된 HTML</p>",
                "{\"extra\":\"updated\"}",
                2002L,
                "update@example.com",
                "010-9999-8888"
        );
        reportRepository.save(found);

        Report updated = reportRepository.findById(found.getId())
                .orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("수정된 제목");
        assertThat(updated.getContentText()).isEqualTo("수정된 텍스트");

        // DELETE
        reportRepository.delete(updated);
        assertThat(reportRepository.findById(updated.getId())).isEmpty();
    }
}