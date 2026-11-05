package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaxFlowTest extends ApiTestSupport {
    @Test
    void defaultIsTenPercentVatIncludedAndAdminCanChangeIt() throws Exception {
        getJson("/api/tax-setting", admin()).andExpect(jsonPath("$.vatRate").value(10))
                .andExpect(jsonPath("$.priceIncludesVat").value(true))
                .andExpect(jsonPath("$.customsNotice").doesNotExist());

        MockHttpSession admin = admin();
        putJson("/api/admin/tax-setting", admin, """
                {"vatRate":10,"priceIncludesVat":true,"customsNotice":"관세는 수령 시 구매자 부담입니다."}
                """).andExpect(status().isOk())
                .andExpect(jsonPath("$.customsNotice").value("관세는 수령 시 구매자 부담입니다."));

        getJson("/api/tax-setting", admin).andExpect(jsonPath("$.customsNotice").value("관세는 수령 시 구매자 부담입니다."));

        putJson("/api/admin/tax-setting", admin, """
                {"vatRate":10,"priceIncludesVat":false,"customsNotice":null}
                """).andExpect(jsonPath("$.priceIncludesVat").value(false))
                .andExpect(jsonPath("$.customsNotice").doesNotExist());
    }
}
