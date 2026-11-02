package com.dgmoonlabs.cms.domain.common.file.constant;

import java.util.Set;

public enum MediaType {
    DOCUMENT, IMAGE, VIDEO, ETC;

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "webm", "mov");
    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of(
            "pdf", "txt", "csv", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "hwp", "hwpx");

    public static MediaType fromExtension(final String extension) {
        if (IMAGE_EXTENSIONS.contains(extension)) {
            return IMAGE;
        }
        if (VIDEO_EXTENSIONS.contains(extension)) {
            return VIDEO;
        }
        if (DOCUMENT_EXTENSIONS.contains(extension)) {
            return DOCUMENT;
        }
        return ETC;
    }
}
