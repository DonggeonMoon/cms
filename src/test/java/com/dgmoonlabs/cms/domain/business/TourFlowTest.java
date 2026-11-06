package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TourFlowTest extends ApiTestSupport {
    private static final String APPLICANT = "\"applicantName\":\"여행자\",\"phoneNumber\":\"010-1234-5678\"";
    private static final LocalDate DAY = LocalDate.now().plusDays(10);
    private static final String PRICE_TIERS = """
            [{"name":"성인","price":100000},{"name":"아동","price":50000}]
            """;

    private long createTour(final MockHttpSession admin, final String priceTiers) throws Exception {
        return createdId(postJson("/api/admin/reservation-items", admin, """
                {"name":"제주도 3박4일","price":0,"reservationType":"TOUR","isEnabled":true,"priceTiers":%s}
                """.formatted(priceTiers)));
    }

    private long departure(final MockHttpSession admin, final long tourId, final int capacity) throws Exception {
        return body(postJson("/api/admin/reservation-items/%d/sessions".formatted(tourId), admin,
                "{\"name\":\"1차 출발\",\"startAt\":\"%sT08:00:00\",\"capacity\":%d}".formatted(DAY, capacity))
                .andExpect(status().isOk())).get("id").asLong();
    }

    @Test
    void priceTiersAreRequiredToCreateATour() throws Exception {
        MockHttpSession admin = admin();
        postJson("/api/admin/reservation-items", admin, """
                {"name":"요금 없는 투어","price":0,"reservationType":"TOUR","isEnabled":true}
                """).andExpect(jsonPath("$.code").value("RESERVATION_SETTINGS_REQUIRED"));
        postJson("/api/admin/reservation-items", admin, """
                {"name":"요금 없는 투어","price":0,"reservationType":"TOUR","isEnabled":true,"priceTiers":[]}
                """).andExpect(jsonPath("$.code").value("RESERVATION_SETTINGS_REQUIRED"));
    }

    @Test
    void partyBreakdownIsValidatedAndPricedByTier() throws Exception {
        MockHttpSession admin = admin();
        long tour = createTour(admin, PRICE_TIERS);
        long departure = departure(admin, tour, 10);
        MockHttpSession member = member("traveler1");

        postJson("/api/reservations", member, """
                {"reservationItemId":%d,"quantity":1,"sessionId":%d,%s}
                """.formatted(tour, departure, APPLICANT)).andExpect(jsonPath("$.code").value("PARTY_BREAKDOWN_REQUIRED"));

        postJson("/api/reservations", member, """
                {"reservationItemId":%d,"quantity":1,"sessionId":%d,"partyCounts":{"대학생":1},%s}
                """.formatted(tour, departure, APPLICANT)).andExpect(jsonPath("$.code").value("INVALID_PARTY_BREAKDOWN"));

        postJson("/api/reservations", member, """
                {"reservationItemId":%d,"quantity":1,"sessionId":%d,"partyCounts":{"성인":2,"아동":1},%s}
                """.formatted(tour, departure, APPLICANT))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.totalPrice").value(250000))
                .andExpect(jsonPath("$.sessionId").value(departure))
                .andExpect(jsonPath("$.startAt").value(DAY + "T08:00:00"))
                .andExpect(jsonPath("$.partyBreakdown.성인").value(2))
                .andExpect(jsonPath("$.partyBreakdown.아동").value(1));

        getJson("/api/reservation-items/" + tour, null)
                .andExpect(jsonPath("$.sessions[0].reservedCount").value(3))
                .andExpect(jsonPath("$.sessions[0].remaining").value(7))
                .andExpect(jsonPath("$.priceTiers[0].name").value("성인"));
    }

    @Test
    void departureCapacityIsSharedAcrossAllPartyTypes() throws Exception {
        MockHttpSession admin = admin();
        long tour = createTour(admin, PRICE_TIERS);
        long departure = departure(admin, tour, 3);

        MockHttpSession traveler2 = member("traveler2");
        long reservationId = createdId(postJson("/api/reservations", traveler2, """
                {"reservationItemId":%d,"quantity":1,"sessionId":%d,"partyCounts":{"성인":2,"아동":1},%s}
                """.formatted(tour, departure, APPLICANT)));

        postJson("/api/reservations", member("traveler3"), """
                {"reservationItemId":%d,"quantity":1,"sessionId":%d,"partyCounts":{"성인":1},%s}
                """.formatted(tour, departure, APPLICANT)).andExpect(jsonPath("$.code").value("RESERVATION_ITEM_FULL"));

        postJson("/api/reservations/%d/cancel".formatted(reservationId), traveler2, "").andExpect(status().isNoContent());
        getJson("/api/reservation-items/" + tour, null).andExpect(jsonPath("$.sessions[0].remaining").value(3));
    }
}
