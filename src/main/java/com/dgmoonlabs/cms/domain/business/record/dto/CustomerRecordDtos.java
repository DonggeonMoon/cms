package com.dgmoonlabs.cms.domain.business.record.dto;

import com.dgmoonlabs.cms.domain.business.record.entity.CustomerRecord;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class CustomerRecordDtos {
    private CustomerRecordDtos() {
    }

    public record CustomerRecordSaveRequest(@NotNull Long memberId, Long reservationId, @NotNull LocalDate recordedOn,
                                            @Size(max = 50) String category, @NotBlank @Size(max = 200) String title,
                                            @Size(max = 10_000) String content, @Size(max = 20) List<@NotNull Long> photoFileIds,
                                            boolean sharedWithMember) {
        public List<Long> photosOrEmpty() {
            return photoFileIds == null ? List.of() : photoFileIds.stream().distinct().toList();
        }
    }

    public record CustomerRecordResponse(Long id, Long memberId, Long reservationId, LocalDate recordedOn, String category,
                                         String title, String content, List<Long> photoFileIds, List<String> photoUrls,
                                         boolean sharedWithMember, String authorName, LocalDateTime createdAt,
                                         LocalDateTime modifiedAt) {
        public static CustomerRecordResponse from(final CustomerRecord record) {
            List<Long> photos = record.getPhotoFileIds() == null ? List.of() : record.getPhotoFileIds();
            return new CustomerRecordResponse(record.getId(), record.getMemberId(), record.getReservationId(), record.getRecordedOn(),
                    record.getCategory(), record.getTitle(), record.getContent(), photos,
                    photos.stream().map(id -> "/api/files/" + id + "/view").toList(), record.isSharedWithMember(),
                    record.getAuthorName(), record.getCreatedAt(), record.getModifiedAt());
        }
    }

    /** 회원에게는 작성한 관리자와 공개 여부를 보여주지 않는다. */
    public record MyCustomerRecord(Long id, Long reservationId, LocalDate recordedOn, String category, String title,
                                   String content, List<String> photoUrls) {
        public static MyCustomerRecord from(final CustomerRecord record) {
            List<Long> photos = record.getPhotoFileIds() == null ? List.of() : record.getPhotoFileIds();
            return new MyCustomerRecord(record.getId(), record.getReservationId(), record.getRecordedOn(), record.getCategory(),
                    record.getTitle(), record.getContent(), photos.stream().map(id -> "/api/files/" + id + "/view").toList());
        }
    }
}
