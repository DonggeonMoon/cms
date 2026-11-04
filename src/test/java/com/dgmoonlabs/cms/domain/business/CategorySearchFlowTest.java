package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategorySearchFlowTest extends ApiTestSupport {
    @Test
    void productsAreFilteredByCategoryTreeAndAttributes() throws Exception {
        MockHttpSession admin = admin();
        long electronics = createdId(postJson("/api/admin/product-categories", admin, """
                {"name":"가전","attributes":[{"key":"brand","label":"브랜드","type":"SELECT","options":["가나","다라"],"filterable":true}]}
                """));
        postJson("/api/admin/product-categories", admin, """
                {"name":"노트북","parentId":%d,"attributes":[{"key":"brand","label":"제조사","type":"TEXT"}]}
                """.formatted(electronics)).andExpect(jsonPath("$.code").value("INVALID_CATEGORY_ATTRIBUTES"));
        long laptop = createdId(postJson("/api/admin/product-categories", admin, """
                {"name":"노트북","parentId":%d,"attributes":[
                  {"key":"weight","label":"무게","type":"NUMBER","unit":"kg","filterable":true},
                  {"key":"cpu","label":"CPU","type":"TEXT"}]}
                """.formatted(electronics)));
        postJson("/api/admin/product-categories", admin, "{\"name\":\"손자\",\"parentId\":%d}".formatted(laptop))
                .andExpect(jsonPath("$.code").value("INVALID_CATEGORY_PARENT"));
        long tv = createdId(postJson("/api/admin/product-categories", admin, "{\"name\":\"TV\",\"parentId\":%d}".formatted(electronics)));

        String laptopProduct = """
                {"name":"%s","price":1000000,"isEnabled":true,"categoryId":%d,"attributes":{"brand":"%s","weight":%s,"cpu":"i7","ghost":"x"}}
                """;
        long light = createdId(postJson("/api/admin/products", admin, laptopProduct.formatted("가벼운 노트북", laptop, "가나", "1.1")));
        createdId(postJson("/api/admin/products", admin, laptopProduct.formatted("무거운 노트북", laptop, "다라", "2.4")));
        createdId(postJson("/api/admin/products", admin, """
                {"name":"큰 TV","price":2000000,"isEnabled":true,"categoryId":%d,"attributes":{"brand":"가나"}}
                """.formatted(tv)));
        createdId(postJson("/api/admin/products", admin, "{\"name\":\"분류 없음\",\"price\":1000,\"isEnabled\":true}"));

        getJson("/api/products/" + light, null)
                .andExpect(jsonPath("$.attributes.ghost").doesNotExist())
                .andExpect(jsonPath("$.specs[0].label").value("브랜드"))
                .andExpect(jsonPath("$.specs[1].unit").value("kg"));
        getJson("/api/products?categoryId=" + electronics, null)
                .andExpect(jsonPath("$.page.totalElements").value(3));
        getJson("/api/products?categoryId=%d&attr.brand=가나".formatted(electronics), null)
                .andExpect(jsonPath("$.content[*].name", containsInAnyOrder("가벼운 노트북", "큰 TV")));
        getJson("/api/products?categoryId=%d&attr.brand=가나&attr.brand=다라&max.weight=2".formatted(laptop), null)
                .andExpect(jsonPath("$.content[*].name", containsInAnyOrder("가벼운 노트북")));
        getJson("/api/products?min.weight=2&keyword=노트북", null)
                .andExpect(jsonPath("$.content[*].name", containsInAnyOrder("무거운 노트북")));
        getJson("/api/products?min.weight=abc", null).andExpect(status().isBadRequest());

        getJson("/api/product-categories", null)
                .andExpect(jsonPath("$[?(@.id == %d)].effectiveAttributes.length()".formatted(laptop)).value(3))
                .andExpect(jsonPath("$[?(@.id == %d)].productCount".formatted(laptop)).value(2));
        deleteJson("/api/admin/product-categories/" + electronics, admin, null).andExpect(jsonPath("$.code").value("CATEGORY_IN_USE"));
        postJson("/api/admin/products", admin, laptopProduct.formatted("잘못", laptop, "없는 브랜드", "1.2"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("attributes.brand"));
    }
}
