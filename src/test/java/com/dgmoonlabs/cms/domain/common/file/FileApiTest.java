package com.dgmoonlabs.cms.domain.common.file;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FileApiTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser
    void uploadAndDownloadDocument() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "보고서.txt", "application/x-evil",
                "hello".getBytes(StandardCharsets.UTF_8));

        String location = mockMvc.perform(multipart("/api/files").file(file).with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mediaType").value("DOCUMENT"))
                .andExpect(jsonPath("$.mimeType").value("text/plain"))
                .andExpect(jsonPath("$.thumbnailUrl").doesNotExist())
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(location + "/download"))
                .andExpect(status().isOk())
                .andExpect(content().string("hello"))
                .andExpect(header().string("Content-Disposition", containsString("attachment")));
        mockMvc.perform(get(location)).andExpect(jsonPath("$.downloadCount").value(1));
    }

    @Test
    @WithMockUser
    void imageUploadCreatesThumbnail() throws Exception {
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(800, 400, BufferedImage.TYPE_INT_RGB), "png", png);

        String location = mockMvc.perform(multipart("/api/files")
                        .file(new MockMultipartFile("file", "photo.PNG", "image/png", png.toByteArray()))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mediaType").value("IMAGE"))
                .andExpect(jsonPath("$.extension").value("png"))
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(location + "/thumbnail"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"));
    }

    @Test
    @WithMockUser
    void rejectsDisallowedExtension() throws Exception {
        mockMvc.perform(multipart("/api/files")
                        .file(new MockMultipartFile("file", "run.sh", "text/plain", "rm -rf".getBytes()))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("FILE_EXTENSION_NOT_ALLOWED"));
    }

    @Test
    void anonymousCannotUpload() throws Exception {
        mockMvc.perform(multipart("/api/files")
                        .file(new MockMultipartFile("file", "a.txt", "text/plain", "x".getBytes()))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
