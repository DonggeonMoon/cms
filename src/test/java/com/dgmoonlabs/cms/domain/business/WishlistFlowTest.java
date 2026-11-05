package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WishlistFlowTest extends ApiTestSupport {
    @Test
    void membersKeepTheirOwnWishlistAndDeletedProductsDropOut() throws Exception {
        MockHttpSession admin = admin();
        long tea = createdId(postJson("/api/admin/products", admin, "{\"name\":\"녹차\",\"price\":3000,\"isEnabled\":true}"));
        long cup = createdId(postJson("/api/admin/products", admin, "{\"name\":\"찻잔\",\"price\":8000,\"isEnabled\":true}"));

        getJson("/api/wishlist", null).andExpect(status().isUnauthorized());
        MockHttpSession member = member("wisher");
        putJson("/api/wishlist/" + tea, member, "").andExpect(status().isNoContent());
        putJson("/api/wishlist/" + tea, member, "").andExpect(status().isNoContent());
        putJson("/api/wishlist/" + cup, member, "").andExpect(status().isNoContent());
        putJson("/api/wishlist/999999", member, "").andExpect(status().isNotFound());

        getJson("/api/wishlist", member)
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].product.name").value("찻잔"));
        getJson("/api/wishlist/product-ids", member("otherwisher")).andExpect(jsonPath("$.length()").value(0));

        deleteJson("/api/admin/products/" + cup, admin, null).andExpect(status().isNoContent());
        getJson("/api/wishlist", member).andExpect(jsonPath("$.content.length()").value(1));

        deleteJson("/api/wishlist/" + tea, member, null).andExpect(status().isNoContent());
        getJson("/api/wishlist/product-ids", member).andExpect(jsonPath("$.length()").value(0));
    }
}
