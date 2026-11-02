package com.dgmoonlabs.cms.domain.common.file;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Set;

@ConfigurationProperties("cms.file")
public record FileProperties(
        @DefaultValue("./uploads") String uploadDir,
        @DefaultValue("320") int thumbnailMaxSize,
        @DefaultValue({"jpg", "jpeg", "png", "gif", "bmp", "webp", "mp4", "webm", "mov",
                "pdf", "txt", "csv", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "hwp", "hwpx", "zip"})
        Set<String> allowedExtensions
) {
}
