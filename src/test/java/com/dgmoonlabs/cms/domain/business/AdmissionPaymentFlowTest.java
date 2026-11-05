package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 선정 후 결제(입학·입회비): 합격한 신청자만, 한 번만 연결한 상품으로 결제 주문을 만들 수 있다. */
class AdmissionPaymentFlowTest extends ApiTestSupport {
    @Test
    void acceptedApplicantPaysAdmissionFeeOnce() throws Exception {
        MockHttpSession admin = admin();
        long feeProduct = createdId(postJson("/api/admin/products", admin, """
                {"name":"입학금","price":300000,"saleType":"PURCHASE","requiresShipping":false,"isEnabled":true}
                """));
        long formId = createdId(postJson("/api/admin/applications", admin, """
                {"title":"신입생 모집","isEnabled":true,"admissionFeeProductId":%d,"fields":[
                  {"key":"name","label":"이름","type":"TEXT","required":true}]}
                """.formatted(feeProduct)));

        MockHttpSession member = member("applicant");
        long submissionId = body(postJson("/api/applications/%d/submissions".formatted(formId), member, """
                {"answers":{"name":"홍길동"}}
                """)).get("id").asLong();

        postJson("/api/applications/submissions/" + submissionId + "/admission-payment", member, "{}")
                .andExpect(jsonPath("$.code").value("SUBMISSION_NOT_ACCEPTED"));

        putJson("/api/admin/applications/submissions/" + submissionId, admin, """
                {"status":"ACCEPTED","resultMessage":"합격을 축하드립니다."}
                """).andExpect(jsonPath("$.status").value("ACCEPTED"));

        long orderId = body(postJson("/api/applications/submissions/" + submissionId + "/admission-payment", member, "{}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.totalPrice").value(300000)))
                .get("id").asLong();

        getJson("/api/applications/me", member)
                .andExpect(jsonPath("$.content[0].admissionOrderId").value(orderId));

        postJson("/api/applications/submissions/" + submissionId + "/admission-payment", member, "{}")
                .andExpect(jsonPath("$.code").value("ADMISSION_ALREADY_ORDERED"));

        long formWithoutFee = createdId(postJson("/api/admin/applications", admin, """
                {"title":"일반 모집","isEnabled":true,"fields":[
                  {"key":"name","label":"이름","type":"TEXT","required":true}]}
                """));
        long otherSubmission = body(postJson("/api/applications/%d/submissions".formatted(formWithoutFee), member, """
                {"answers":{"name":"홍길동"}}
                """)).get("id").asLong();
        putJson("/api/admin/applications/submissions/" + otherSubmission, admin, """
                {"status":"ACCEPTED"}
                """).andExpect(status().isOk());
        postJson("/api/applications/submissions/" + otherSubmission + "/admission-payment", member, "{}")
                .andExpect(jsonPath("$.code").value("ADMISSION_FEE_NOT_CONFIGURED"));
    }
}
