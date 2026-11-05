package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.domain.business.order.dto.ProductDtos.ProductDetail;
import com.dgmoonlabs.cms.domain.business.order.dto.ProductDtos.ProductSummary;
import com.dgmoonlabs.cms.domain.business.order.service.AttributeFilters;
import com.dgmoonlabs.cms.domain.business.order.service.ProductService;
import com.dgmoonlabs.cms.domain.business.reservation.dto.ReservationDtos.Availability;
import com.dgmoonlabs.cms.domain.business.reservation.dto.ReservationDtos.ProgramResponse;
import com.dgmoonlabs.cms.domain.business.reservation.service.ReservationService;
import com.dgmoonlabs.cms.domain.business.review.dto.ReviewDtos.ReviewResponse;
import com.dgmoonlabs.cms.domain.business.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class ShopController {
    private final ProductService productService;
    private final ReservationService reservationService;
    private final ReviewService reviewService;

    @GetMapping("/api/products")
    public ResponseEntity<PagedModel<ProductSummary>> products(@RequestParam(required = false) final String keyword,
                                                               @RequestParam(required = false) final Long categoryId,
                                                               @RequestParam final MultiValueMap<String, String> params,
                                                               @PageableDefault(size = 12) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(productService.getProducts(keyword, categoryId,
                AttributeFilters.from(params), pageable)));
    }

    @GetMapping("/api/products/{id}")
    public ResponseEntity<ProductDetail> product(@PathVariable final Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @GetMapping("/api/products/{id}/reviews")
    public ResponseEntity<PagedModel<ReviewResponse>> reviews(@PathVariable final Long id,
                                                              @PageableDefault final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(reviewService.getReviews(id, pageable)));
    }

    @GetMapping("/api/programs")
    public ResponseEntity<PagedModel<ProgramResponse>> programs(@RequestParam(required = false) final String keyword,
                                                                @PageableDefault(size = 12) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(reservationService.getPrograms(keyword, pageable)));
    }

    @GetMapping("/api/programs/{id}")
    public ResponseEntity<ProgramResponse> program(@PathVariable final Long id) {
        return ResponseEntity.ok(reservationService.getProgram(id));
    }

    @GetMapping("/api/programs/{id}/availability")
    public ResponseEntity<Availability> availability(
            @PathVariable final Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime to,
            @RequestParam(required = false) final Long sessionId) {
        return ResponseEntity.ok(reservationService.availability(id, date, from, to, sessionId));
    }

    @GetMapping("/api/programs/{id}/reviews")
    public ResponseEntity<PagedModel<ReviewResponse>> programReviews(@PathVariable final Long id,
                                                                     @PageableDefault final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(reviewService.getProgramReviews(id, pageable)));
    }
}
