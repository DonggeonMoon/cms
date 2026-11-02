package com.dgmoonlabs.cms.domain.common.file.service;

import com.dgmoonlabs.cms.domain.common.file.FileProperties;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class FileStorage {
    private static final DateTimeFormatter DIRECTORY_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final Path root;
    private final int thumbnailMaxSize;

    public FileStorage(final FileProperties properties) {
        this.root = Path.of(properties.uploadDir()).toAbsolutePath().normalize();
        this.thumbnailMaxSize = properties.thumbnailMaxSize();
    }

    public String store(final InputStream inputStream, final String storedName) {
        String relativePath = LocalDate.now().format(DIRECTORY_FORMAT) + "/" + storedName;
        Path target = resolve(relativePath);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            return relativePath;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_STORAGE_FAILED, e);
        }
    }

    public ImageInfo createThumbnail(final String relativePath, final String extension) {
        try {
            BufferedImage original = ImageIO.read(resolve(relativePath).toFile());
            if (original == null) {
                return null;
            }

            double scale = Math.min(1.0, (double) thumbnailMaxSize / Math.max(original.getWidth(), original.getHeight()));
            int width = Math.max(1, (int) Math.round(original.getWidth() * scale));
            int height = Math.max(1, (int) Math.round(original.getHeight() * scale));

            boolean keepsAlpha = !extension.equals("jpg") && !extension.equals("jpeg");
            BufferedImage thumbnail = new BufferedImage(width, height,
                    keepsAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = thumbnail.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(original, 0, 0, width, height, null);
            graphics.dispose();

            String thumbnailPath = relativePath.replaceFirst("(\\.[^./]+)?$", "_thumb.png");
            ImageIO.write(thumbnail, "png", resolve(thumbnailPath).toFile());
            return new ImageInfo(thumbnailPath, original.getWidth(), original.getHeight(), width, height);
        } catch (IOException e) {
            return null;
        }
    }

    public Resource load(final String relativePath) {
        Path path = resolve(relativePath);
        if (!Files.isRegularFile(path)) {
            throw BusinessException.notFound();
        }
        return new FileSystemResource(path);
    }

    private Path resolve(final String relativePath) {
        Path resolved = root.resolve(relativePath).normalize();
        if (!resolved.startsWith(root)) {
            throw BusinessException.notFound();
        }
        return resolved;
    }

    public record ImageInfo(String thumbnailPath, int originalWidth, int originalHeight,
                            int thumbnailWidth, int thumbnailHeight) {
    }
}
