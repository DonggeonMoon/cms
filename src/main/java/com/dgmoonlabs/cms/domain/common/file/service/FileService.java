package com.dgmoonlabs.cms.domain.common.file.service;

import com.dgmoonlabs.cms.domain.common.file.FileProperties;
import com.dgmoonlabs.cms.domain.common.file.constant.MediaType;
import com.dgmoonlabs.cms.domain.common.file.dto.FileResponse;
import com.dgmoonlabs.cms.domain.common.file.entity.File;
import com.dgmoonlabs.cms.domain.common.file.entity.ImageFile;
import com.dgmoonlabs.cms.domain.common.file.repository.FileRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;
    private final FileStorage fileStorage;
    private final FileProperties fileProperties;

    @Transactional
    public FileResponse upload(final MultipartFile multipartFile, final String alternativeText) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_FILE);
        }
        String originalName = StringUtils.getFilename(StringUtils.cleanPath(
                multipartFile.getOriginalFilename() == null ? "" : multipartFile.getOriginalFilename()));
        String extension = extensionOf(originalName);
        if (!fileProperties.allowedExtensions().contains(extension)) {
            throw new BusinessException(ErrorCode.FILE_EXTENSION_NOT_ALLOWED);
        }

        String uuid = UUID.randomUUID().toString();
        String path;
        try (InputStream inputStream = multipartFile.getInputStream()) {
            path = fileStorage.store(inputStream, uuid + "." + extension);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_STORAGE_FAILED, e);
        }

        MediaType mediaType = MediaType.fromExtension(extension);
        String mimeType = MediaTypeFactory.getMediaType(originalName)
                .map(Object::toString)
                .orElse(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE);

        File file = mediaType == MediaType.IMAGE
                ? buildImageFile(path, extension, originalName, uuid, mimeType, multipartFile.getSize(), alternativeText)
                : File.builder()
                .originalName(originalName)
                .uuid(uuid)
                .path(path)
                .extension(extension)
                .mimeType(mimeType)
                .mediaType(mediaType)
                .size(multipartFile.getSize())
                .alternativeText(alternativeText)
                .build();

        return FileResponse.from(fileRepository.save(file));
    }

    @Transactional(readOnly = true)
    public FileResponse getFile(final Long id) {
        return FileResponse.from(findPublicFile(id));
    }

    @Transactional(readOnly = true)
    public Page<FileResponse> searchFiles(final String keyword, final MediaType mediaType, final Pageable pageable) {
        return fileRepository.search(keyword, mediaType, pageable).map(FileResponse::from);
    }

    @Transactional(readOnly = true)
    public List<FileResponse> getFiles(final List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, File> files = fileRepository.findAllById(ids).stream()
                .filter(file -> file.getDeletedAt() == null)
                .collect(Collectors.toMap(File::getId, Function.identity()));
        return ids.stream().map(files::get).filter(Objects::nonNull).map(FileResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public void requireFilesExist(final Collection<Long> ids) {
        if (getFiles(List.copyOf(new LinkedHashSet<>(ids))).size() != new HashSet<>(ids).size()) {
            throw BusinessException.notFound();
        }
    }

    @Transactional
    public DownloadableFile download(final Long id) {
        return downloadFile(findPublicFile(id));
    }

    /** 내려받을 권한을 호출한 쪽에서 이미 확인한 보호 파일을 내려준다. */
    @Transactional
    public DownloadableFile downloadProtected(final Long id) {
        return downloadFile(findFile(id));
    }

    @Transactional(readOnly = true)
    public DownloadableFile viewProtected(final Long id) {
        File file = findFile(id);
        return new DownloadableFile(fileStorage.load(file.getPath()), file.getOriginalName(), file.getMimeType());
    }

    @Transactional
    public void protect(final Collection<Long> ids) {
        fileRepository.findAllById(ids).forEach(File::protect);
    }

    private DownloadableFile downloadFile(final File file) {
        file.increaseDownloadCount();
        return new DownloadableFile(fileStorage.load(file.getPath()), file.getOriginalName(), file.getMimeType());
    }

    @Transactional(readOnly = true)
    public DownloadableFile view(final Long id) {
        File file = findPublicFile(id);
        return new DownloadableFile(fileStorage.load(file.getPath()), file.getOriginalName(), file.getMimeType());
    }

    @Transactional(readOnly = true)
    public DownloadableFile thumbnail(final Long id) {
        if (!(findPublicFile(id) instanceof ImageFile image) || image.getThumbnailPath() == null) {
            throw BusinessException.notFound();
        }
        return new DownloadableFile(fileStorage.load(image.getThumbnailPath()), "thumbnail.png", "image/png");
    }

    @Transactional
    public void deleteFile(final Long id) {
        findFile(id).delete();
    }

    private File buildImageFile(final String path, final String extension, final String originalName, final String uuid,
                                final String mimeType, final long size, final String alternativeText) {
        FileStorage.ImageInfo info = fileStorage.createThumbnail(path, extension);
        return ImageFile.builder()
                .originalName(originalName)
                .uuid(uuid)
                .path(path)
                .extension(extension)
                .mimeType(mimeType)
                .mediaType(MediaType.IMAGE)
                .size(size)
                .alternativeText(alternativeText)
                .thumbnailPath(info == null ? null : info.thumbnailPath())
                .originalWidth(info == null ? 0 : info.originalWidth())
                .originalHeight(info == null ? 0 : info.originalHeight())
                .thumbnailWidth(info == null ? 0 : info.thumbnailWidth())
                .thumbnailHeight(info == null ? 0 : info.thumbnailHeight())
                .build();
    }

    private File findPublicFile(final Long id) {
        File file = findFile(id);
        if (file.isProtected() && !CurrentUser.isAdmin()) {
            throw BusinessException.notFound();
        }
        return file;
    }

    private File findFile(final Long id) {
        return fileRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }

    private static String extensionOf(final String filename) {
        String extension = StringUtils.getFilenameExtension(filename);
        return extension == null ? "" : extension.toLowerCase(Locale.ROOT);
    }

    public record DownloadableFile(Resource resource, String filename, String mimeType) {
    }
}
