package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReservationItemSessionFlowTest extends ApiTestSupport {
    private static final String APPLICANT = "\"applicantName\":\"수강생\",\"phoneNumber\":\"010-1234-5678\"";
    private static final LocalDate DAY = LocalDate.now().plusDays(7);
    private static final String LAYOUT = """
            {"rows":1,"cols":2,"cells":[
              {"key":"A1","row":0,"col":0,"kind":"SEAT","label":"A1"},
              {"key":"A2","row":0,"col":1,"kind":"SEAT","label":"A2"}]}
            """;

    private long session(final MockHttpSession admin, final long reservationItemId, final String body) throws Exception {
        return body(postJson("/api/admin/reservation-items/%d/sessions".formatted(reservationItemId), admin, body)
                .andExpect(status().isOk())).get("id").asLong();
    }

    @Test
    void eachClassHasItsOwnCapacity() throws Exception {
        MockHttpSession admin = admin();
        long yoga = createdId(postJson("/api/admin/reservation-items", admin, """
                {"name":"요가","price":20000,"reservationType":"PROGRAM","isEnabled":true}
                """));
        long tuesday = session(admin, yoga, "{\"name\":\"화요일 저녁반\",\"startAt\":\"%sT19:00:00\",\"endAt\":\"%sT20:00:00\",\"capacity\":2}"
                .formatted(DAY, DAY));
        long thursday = session(admin, yoga, "{\"name\":\"목요일 오전반\",\"startAt\":\"%sT10:00:00\",\"capacity\":1}".formatted(DAY.plusDays(2)));
        long past = session(admin, yoga, "{\"startAt\":\"%sT10:00:00\"}".formatted(LocalDate.now().minusDays(1)));
        postJson("/api/admin/reservation-items/%d/sessions".formatted(yoga), admin, "{\"startAt\":\"%sT10:00:00\",\"endAt\":\"%sT09:00:00\"}"
                .formatted(DAY, DAY)).andExpect(jsonPath("$.code").value("INVALID_PERIOD"));

        String book = "{\"reservationItemId\":%d,\"quantity\":%d,%s%s}";
        MockHttpSession member = member("yogi");
        postJson("/api/reservations", member, book.formatted(yoga, 1, APPLICANT, ""))
                .andExpect(jsonPath("$.code").value("SESSION_REQUIRED"));
        postJson("/api/reservations", member, book.formatted(yoga, 1, APPLICANT, ",\"sessionId\":" + past))
                .andExpect(jsonPath("$.code").value("SESSION_NOT_OPEN"));
        long reservationId = body(postJson("/api/reservations", member, book.formatted(yoga, 2, APPLICANT, ",\"sessionId\":" + tuesday))
                .andExpect(jsonPath("$.totalPrice").value(40000))
                .andExpect(jsonPath("$.sessionId").value(tuesday))
                .andExpect(jsonPath("$.startAt").value(DAY + "T19:00:00"))).get("id").asLong();
        postJson("/api/reservations", member("yogi2"), book.formatted(yoga, 1, APPLICANT, ",\"sessionId\":" + tuesday))
                .andExpect(jsonPath("$.code").value("RESERVATION_ITEM_FULL"));
        postJson("/api/reservations", member("yogi3"), book.formatted(yoga, 1, APPLICANT, ",\"sessionId\":" + thursday))
                .andExpect(status().isCreated());

        getJson("/api/reservation-items/" + yoga, null)
                .andExpect(jsonPath("$.sessions.length()").value(3))
                .andExpect(jsonPath("$.sessions[0].isOpen").value(false))
                .andExpect(jsonPath("$.sessions[1].remaining").value(0))
                .andExpect(jsonPath("$.sessions[2].remaining").value(0));

        putJson("/api/admin/reservation-items/%d/sessions/%d".formatted(yoga, tuesday), admin,
                "{\"startAt\":\"%sT19:00:00\",\"capacity\":1}".formatted(DAY)).andExpect(jsonPath("$.code").value("RESERVATION_ITEM_FULL"));
        deleteJson("/api/admin/reservation-items/%d/sessions/%d".formatted(yoga, tuesday), admin, null)
                .andExpect(jsonPath("$.code").value("SESSION_HAS_RESERVATIONS"));

        postJson("/api/reservations/%d/cancel".formatted(reservationId), member, "").andExpect(status().isNoContent());
        getJson("/api/reservation-items/" + yoga, null).andExpect(jsonPath("$.sessions[1].remaining").value(2));
        deleteJson("/api/admin/reservation-items/%d/sessions/%d".formatted(yoga, tuesday), admin, null).andExpect(status().isNoContent());
        getJson("/api/reservation-items/" + yoga, null).andExpect(jsonPath("$.sessions.length()").value(2));
    }

    @Test
    void eventSeatsAreSoldPerShowing() throws Exception {
        MockHttpSession admin = admin();
        long show = createdId(postJson("/api/admin/reservation-items", admin, """
                {"name":"뮤지컬","price":50000,"reservationType":"EVENT","startDateTime":"%sT19:00:00","layout":%s,"isEnabled":true}
                """.formatted(DAY, LAYOUT)));
        long first = session(admin, show, "{\"name\":\"1회차\",\"startAt\":\"%sT14:00:00\",\"endAt\":\"%sT16:00:00\"}".formatted(DAY, DAY));
        long second = session(admin, show, "{\"name\":\"2회차\",\"startAt\":\"%sT19:00:00\",\"endAt\":\"%sT21:00:00\"}".formatted(DAY, DAY));

        String seat = "{\"reservationItemId\":%d,\"quantity\":1,\"cells\":[\"A1\"],\"sessionId\":%d,%s}";
        postJson("/api/reservations", member("fan1"), seat.formatted(show, first, APPLICANT)).andExpect(status().isCreated());
        postJson("/api/reservations", member("fan2"), seat.formatted(show, first, APPLICANT))
                .andExpect(jsonPath("$.code").value("CELL_ALREADY_RESERVED"));
        postJson("/api/reservations", member("fan3"), seat.formatted(show, second, APPLICANT))
                .andExpect(jsonPath("$.startAt").value(DAY + "T19:00:00"));

        getJson("/api/reservation-items/%d/availability?sessionId=%d".formatted(show, first), null)
                .andExpect(jsonPath("$.takenCells[0]").value("A1"));
        getJson("/api/reservation-items/" + show, null).andExpect(jsonPath("$.sessions[0].reservedCount").value(1));

        long consultation = createdId(postJson("/api/admin/reservation-items", admin, """
                {"name":"상담","price":0,"reservationType":"CONSULTATION","slotMinutes":30,"openTime":"09:00","closeTime":"12:00",
                 "startDateTime":"%sT00:00:00","endDateTime":"%sT00:00:00","isEnabled":true}
                """.formatted(DAY, DAY.plusDays(3))));
        postJson("/api/admin/reservation-items/%d/sessions".formatted(consultation), admin, "{\"startAt\":\"%sT10:00:00\"}".formatted(DAY))
                .andExpect(jsonPath("$.code").value("SESSIONS_NOT_SUPPORTED"));
    }
}
