package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InquiryFlowTest extends ApiTestSupport {
    private static final String APPLICANT = "\"applicantName\":\"문의자\",\"phoneNumber\":\"010-1234-5678\"";
    private static final LocalDate DAY = LocalDate.now().plusDays(3);

    @Test
    void inquiryOnlyProductLinksToConsultationButCannotBeOrdered() throws Exception {
        MockHttpSession admin = admin();
        postJson("/api/admin/products", admin, """
                {"name":"강남 아파트 매물","price":0,"saleType":"INQUIRY","isEnabled":true}
                """).andExpect(jsonPath("$.code").value("INQUIRY_PROGRAM_REQUIRED"));

        long consultation = createdId(postJson("/api/admin/reservation-items", admin, """
                {"name":"매물 상담","price":0,"reservationType":"CONSULTATION","slotMinutes":30,"maximumPerson":1,
                 "openTime":"10:00","closeTime":"11:00","startDateTime":"%sT00:00:00","endDateTime":"%sT23:59:00","isEnabled":true}
                """.formatted(DAY.minusDays(5), DAY.plusDays(5))));

        long listing = createdId(postJson("/api/admin/products", admin, """
                {"name":"강남 아파트 매물","price":0,"saleType":"INQUIRY","isEnabled":true,"inquiryProgramId":%d}
                """.formatted(consultation)));

        getJson("/api/products/%d".formatted(listing), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inquiry.programId").value(consultation));

        MockHttpSession member = member("inquirer");
        postJson("/api/orders", member, """
                {"productId":%d,"quantity":1,"receiverName":"문의자","phoneNumber":"010-1234-5678"}
                """.formatted(listing)).andExpect(jsonPath("$.code").value("INQUIRY_ONLY_PRODUCT"));

        postJson("/api/reservations", member, """
                {"programId":%d,"quantity":1,"startAt":"%sT10:00:00",%s}
                """.formatted(consultation, DAY, APPLICANT)).andExpect(status().isCreated());
    }
}
