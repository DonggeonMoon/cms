package com.dgmoonlabs.cms.domain.common.file.dto;

import com.dgmoonlabs.cms.domain.common.file.constant.MediaType;
import com.dgmoonlabs.cms.domain.common.file.entity.File;
import com.dgmoonlabs.cms.domain.common.file.entity.ImageFile;

import java.time.LocalDateTime;

public record FileResponse(
        Long id,
        String originalName,
        String extension,
        String mimeType,
        MediaType mediaType,
        Long size,
        String alternativeText,
        int downloadCount,
        String downloadUrl,
        String thumbnailUrl,
        LocalDateTime createdAt
) {
    public static FileResponse from(final File file) {
        String thumbnailUrl = file instanceof ImageFile image && image.getThumbnailPath() != null
                ? "/api/files/" + file.getId() + "/thumbnail"
                : null;
        return new FileResponse(
                file.getId(),
                file.getOriginalName(),
                file.getExtension(),
                file.getMimeType(),
                file.getMediaType(),
                file.getSize(),
                file.getAlternativeText(),
                file.getDownloadCount(),
                "/api/files/" + file.getId() + "/download",
                thumbnailUrl,
                file.getCreatedAt()
        );
    }
}
