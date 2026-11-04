package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReservationTypeFlowTest extends ApiTestSupport {
    private static final String APPLICANT = "\"applicantName\":\"예약자\",\"phoneNumber\":\"010-1234-5678\"";
    private static final LocalDate DAY = LocalDate.now().plusDays(3);
    private static final String LAYOUT = """
            {"rows":1,"cols":3,"cells":[
              {"key":"A1","row":0,"col":0,"kind":"%s","label":"A1"},
              {"key":"A2","row":0,"col":1,"kind":"%s","label":"A2","price":20000},
              {"key":"X","row":0,"col":2,"kind":"AISLE","label":""}]}
            """;

    @Test
    void consultationSlotsFillUpByCapacity() throws Exception {
        MockHttpSession admin = admin();
        postJson("/api/admin/programs", admin, """
                {"name":"진로 상담","price":0,"reservationType":"CONSULTATION","isEnabled":true}
                """).andExpect(jsonPath("$.code").value("RESERVATION_SETTINGS_REQUIRED"));
        long programId = createdId(postJson("/api/admin/programs", admin, """
                {"name":"진로 상담","price":0,"reservationType":"CONSULTATION","slotMinutes":30,"maximumPerson":1,
                 "openTime":"10:00","closeTime":"11:00","startDateTime":"%sT00:00:00","endDateTime":"%sT23:59:00","isEnabled":true}
                """.formatted(DAY.minusDays(5), DAY.plusDays(5))).andExpect(status().isCreated()));
        getJson("/api/programs/" + programId + "/availability?date=" + DAY, null)
                .andExpect(jsonPath("$.slots.length()").value(2))
                .andExpect(jsonPath("$.slots[0].remaining").value(1));

        MockHttpSession member = member("consult1");
        postJson("/api/reservations", member, """
                {"programId":%d,"quantity":1,"startAt":"%sT10:15:00",%s}
                """.formatted(programId, DAY, APPLICANT)).andExpect(jsonPath("$.code").value("INVALID_RESERVATION_TIME"));
        postJson("/api/reservations", member, """
                {"programId":%d,"quantity":1,"startAt":"%sT10:00:00",%s}
                """.formatted(programId, DAY, APPLICANT))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.endAt").value(DAY + "T10:30:00"));
        postJson("/api/reservations", member("consult2"), """
                {"programId":%d,"quantity":1,"startAt":"%sT10:00:00",%s}
                """.formatted(programId, DAY, APPLICANT)).andExpect(jsonPath("$.code").value("RESERVATION_SLOT_FULL"));
    }

    @Test
    void eventSeatsAreBookedOnceAndPricedPerSeat() throws Exception {
        MockHttpSession admin = admin();
        long programId = createdId(postJson("/api/admin/programs", admin, """
                {"name":"재즈 공연","price":10000,"reservationType":"EVENT","startDateTime":"%sT19:00:00",
                 "layout":%s,"isEnabled":true}
                """.formatted(DAY, LAYOUT.formatted("SEAT", "SEAT"))).andExpect(status().isCreated()));
        getJson("/api/programs/" + programId, null).andExpect(jsonPath("$.layout.cells[1].price").value(20000));

        MockHttpSession member = member("jazz1");
        postJson("/api/reservations", member, """
                {"programId":%d,"quantity":1,"cells":["X"],%s}
                """.formatted(programId, APPLICANT)).andExpect(jsonPath("$.code").value("INVALID_CELL_SELECTION"));
        postJson("/api/reservations", member, """
                {"programId":%d,"quantity":1,"cells":["A1","A2"],%s}
                """.formatted(programId, APPLICANT))
                .andExpect(jsonPath("$.totalPrice").value(30000))
                .andExpect(jsonPath("$.quantity").value(2));
        getJson("/api/programs/" + programId + "/availability", null).andExpect(jsonPath("$.takenCells.length()").value(2));
        postJson("/api/reservations", member("jazz2"), """
                {"programId":%d,"quantity":1,"cells":["A2"],%s}
                """.formatted(programId, APPLICANT)).andExpect(jsonPath("$.code").value("CELL_ALREADY_RESERVED"));
    }

    @Test
    void lodgingRoomsCanBeRebookedOnlyForOtherNights() throws Exception {
        MockHttpSession admin = admin();
        long id = createdId(postJson("/api/admin/programs", admin, """
                {"name":"게스트하우스","price":50000,"reservationType":"LODGING","startDateTime":"%sT00:00:00",
                 "endDateTime":"%sT00:00:00","layout":%s,"isEnabled":true}
                """.formatted(DAY.minusDays(10), DAY.plusDays(30), LAYOUT.formatted("ROOM", "ROOM"))).andExpect(status().isCreated()));
        MockHttpSession member = member("guest1");
        postJson("/api/reservations", member, """
                {"programId":%d,"quantity":1,"cells":["A1"],"startAt":"%sT00:00:00","endAt":"%sT00:00:00",%s}
                """.formatted(id, DAY, DAY.plusDays(2), APPLICANT)).andExpect(jsonPath("$.totalPrice").value(100000));
        postJson("/api/reservations", member("guest2"), """
                {"programId":%d,"quantity":1,"cells":["A1"],"startAt":"%sT00:00:00","endAt":"%sT00:00:00",%s}
                """.formatted(id, DAY.plusDays(1), DAY.plusDays(3), APPLICANT)).andExpect(jsonPath("$.code").value("CELL_ALREADY_RESERVED"));
        postJson("/api/reservations", member("guest3"), """
                {"programId":%d,"quantity":1,"cells":["A1"],"startAt":"%sT00:00:00","endAt":"%sT00:00:00",%s}
                """.formatted(id, DAY.plusDays(2), DAY.plusDays(3), APPLICANT)).andExpect(status().isCreated());
    }

    @Test
    void rentalChargesPerTimeUnit() throws Exception {
        MockHttpSession admin = admin();
        long id = createdId(postJson("/api/admin/programs", admin, """
                {"name":"세미나실","price":10000,"reservationType":"RENTAL","slotMinutes":60,"openTime":"09:00","closeTime":"18:00",
                 "startDateTime":"%sT00:00:00","endDateTime":"%sT23:59:00","layout":%s,"isEnabled":true}
                """.formatted(DAY.minusDays(1), DAY.plusDays(1), LAYOUT.formatted("SPACE", "SPACE"))));
        postJson("/api/reservations", member("rent1"), """
                {"programId":%d,"quantity":1,"cells":["A1"],"startAt":"%sT10:00:00","endAt":"%sT13:00:00",%s}
                """.formatted(id, DAY, DAY, APPLICANT)).andExpect(jsonPath("$.totalPrice").value(30000));
        getJson("/api/programs/%d/availability?date=%s&from=%sT12:00:00&to=%sT14:00:00".formatted(id, DAY, DAY, DAY), null)
                .andExpect(jsonPath("$.takenCells[0]").value("A1"))
                .andExpect(jsonPath("$.slots.length()").value(9));
    }
}
