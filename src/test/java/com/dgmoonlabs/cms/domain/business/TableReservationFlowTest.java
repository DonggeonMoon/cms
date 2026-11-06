package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TableReservationFlowTest extends ApiTestSupport {
    private static final String APPLICANT = "\"applicantName\":\"예약자\",\"phoneNumber\":\"010-1234-5678\"";
    private static final LocalDate DAY = LocalDate.now().plusDays(3);
    private static final String LAYOUT = """
            {"rows":1,"cols":2,"cells":[
              {"key":"T1","row":0,"col":0,"kind":"ROOM","label":"1번 테이블","capacity":4},
              {"key":"T2","row":0,"col":1,"kind":"ROOM","label":"2번 테이블","capacity":2}]}
            """;

    private long createReservationItem() throws Exception {
        return createdId(postJson("/api/admin/reservation-items", admin(), """
                {"name":"식당 예약","price":0,"reservationType":"TABLE","slotMinutes":30,"openTime":"11:00","closeTime":"21:00",
                 "startDateTime":"%sT00:00:00","endDateTime":"%sT23:59:00","layout":%s,"isEnabled":true}
                """.formatted(DAY.minusDays(1), DAY.plusDays(1), LAYOUT)).andExpect(status().isCreated()));
    }

    @Test
    void partySizeIsRequiredAndCheckedAgainstTableCapacity() throws Exception {
        long id = createReservationItem();
        MockHttpSession member = member("diner1");
        postJson("/api/reservations", member, """
                {"reservationItemId":%d,"quantity":1,"cells":["T2"],"startAt":"%sT12:00:00","endAt":"%sT12:30:00",%s}
                """.formatted(id, DAY, DAY, APPLICANT)).andExpect(jsonPath("$.code").value("PARTY_SIZE_REQUIRED"));
        postJson("/api/reservations", member, """
                {"reservationItemId":%d,"quantity":1,"cells":["T2"],"startAt":"%sT12:00:00","endAt":"%sT12:30:00","partySize":3,%s}
                """.formatted(id, DAY, DAY, APPLICANT)).andExpect(jsonPath("$.code").value("TABLE_CAPACITY_EXCEEDED"));
    }

    @Test
    void successfulBookingStoresPartySizeAndTableIsTakenForThatSlot() throws Exception {
        long id = createReservationItem();
        postJson("/api/reservations", member("diner2"), """
                {"reservationItemId":%d,"quantity":1,"cells":["T1"],"startAt":"%sT12:00:00","endAt":"%sT13:00:00","partySize":4,%s}
                """.formatted(id, DAY, DAY, APPLICANT))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.partySize").value(4))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
        getJson("/api/reservation-items/%d/availability?date=%s&from=%sT12:00:00&to=%sT13:00:00".formatted(id, DAY, DAY, DAY), null)
                .andExpect(jsonPath("$.takenCells[0]").value("T1"));
        postJson("/api/reservations", member("diner3"), """
                {"reservationItemId":%d,"quantity":1,"cells":["T1"],"startAt":"%sT12:00:00","endAt":"%sT12:30:00","partySize":2,%s}
                """.formatted(id, DAY, DAY, APPLICANT)).andExpect(jsonPath("$.code").value("CELL_ALREADY_RESERVED"));
    }

    @Test
    void combiningTwoTablesAddsUpTheirCapacity() throws Exception {
        long id = createReservationItem();
        postJson("/api/reservations", member("diner4"), """
                {"reservationItemId":%d,"quantity":1,"cells":["T1","T2"],"startAt":"%sT18:00:00","endAt":"%sT19:00:00","partySize":6,%s}
                """.formatted(id, DAY, DAY, APPLICANT))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(2));
    }
}
