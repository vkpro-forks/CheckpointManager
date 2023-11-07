package ru.ac.checkpointmanager.service.avatar;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.exception.AvatarIsEmptyException;
import ru.ac.checkpointmanager.exception.AvatarNotFoundException;
import ru.ac.checkpointmanager.exception.BadAvatarExtensionException;
import ru.ac.checkpointmanager.model.Avatar;
import ru.ac.checkpointmanager.model.AvatarProperties;
import ru.ac.checkpointmanager.repository.AvatarRepository;
import ru.ac.checkpointmanager.utils.MethodLog;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.UUID;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AvatarServiceImpl implements AvatarService {

    private final AvatarRepository repository;
    private final AvatarProperties avatarProperties;

    @Value("${avatars.dir.path}")
    private String avatarsDir;

    @Value("${avatars.extensions}")
    private String extensions;

    @Value("${avatars.image.max-width}")
    private int maxWidth;

    @Value("${avatars.image.max-height}")
    private int maxHeight;

    @Value("${avatars.max-size}")
    private String maxFileSize;

    @Override
    public Avatar uploadAvatar(UUID entityID, MultipartFile avatarFile) throws IOException {
        log.info("Method uploadAvatar invoked for entityID: {}", entityID);

        logWhenMethodInvoked(MethodLog.getMethodName());
        validateAvatar(avatarFile);
        log.debug("Avatar file validated successfully.");

        // Создаем новый объект Avatar
        Avatar avatar = new Avatar();
        avatar.setFileSize(avatarFile.getSize());
        avatar.setMediaType(avatarFile.getContentType());
        log.debug("Created Avatar object with fileSize: {} and mediaType: {}", avatarFile.getSize(), avatarFile.getContentType());

        // Проверяем размер файла
        if (avatarFile.getSize() > avatarProperties.getMaxSizeInBytes()) {
            log.warn("File size {} exceeds the maximum allowed size of {}", avatarFile.getSize(), maxFileSize);
            throw new IOException("Слишком большой файл");
        }

        // Читаем изображение из файла
        BufferedImage image = ImageIO.read(avatarFile.getInputStream());
        if (image == null) {
            log.warn("The file uploaded for entityID: {} is not an image.", entityID);
            throw new IllegalArgumentException("Файл не является изображением.");
        }

        // Проверяем разрешение изображения
        if (image.getWidth() > maxWidth || image.getHeight() > maxHeight) {
            // Сохраняем большое изображение в файловой системе
            log.debug("The image size is larger than allowed, resizing is required.");
            Path filePath = saveImageToFileSystem(entityID, avatarFile);
            avatar.setFilePath(filePath.toString());
            log.info("Large image saved to filesystem for entityID: {}", entityID);

            // Создаем уменьшенную версию изображения для БД
            BufferedImage resizedImage = resizeImage(image, maxWidth, maxHeight);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "png", baos);
            avatar.setPreview(baos.toByteArray());
            log.debug("Resized image created and set as avatar preview for entityID: {}", entityID);
        } else {
            // Сохраняем маленькое изображение напрямую в БД
            log.debug("Image is within the allowed size, saving directly to the database.");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            avatar.setPreview(baos.toByteArray());
        }

        log.info("Avatar object saved in the repository for entityID: {}", entityID);
        return repository.save(avatar);
    }

    @Override
    public void getAvatar(UUID entityID, HttpServletResponse response) throws IOException {
        log.debug("Fetching avatar for entity ID: {}", entityID);
        Avatar avatar = findAvatarOrThrow(entityID);

        if (avatar.getFilePath() != null && !avatar.getFilePath().isEmpty()) {
            // Загружаем файл из файловой системы
            Path filePath = Paths.get(avatar.getFilePath());
            File file = filePath.toFile();
            response.setContentType(avatar.getMediaType());
            response.setContentLength((int) file.length());
            Files.copy(filePath, response.getOutputStream());
            log.info("Avatar served from file system for entity ID: {}", entityID);
        } else {
            // Отправляем предварительный просмотр из базы данных
            response.setContentType(avatar.getMediaType());
            response.getOutputStream().write(avatar.getPreview());
            log.info("Preview avatar served from database for entity ID: {}", entityID);
        }
    }


    @Override
    public Avatar deleteAvatarIfExists(UUID entityID) {
        log.debug("Attempting to delete avatar for entity ID: {}", entityID);
        Avatar avatar = findAvatarOrThrow(entityID);
        if (avatar != null) {
            repository.delete(avatar);
            log.info("Avatar deleted for entity ID: {}", entityID);
            return avatar;
        } else {
            log.info("No avatar found for entity ID: {}, nothing to delete", entityID);
            return null;
        }
    }


    @Override
    public Avatar findAvatarOrThrow(UUID entityID) {
        return repository.findById(entityID)
                .orElseThrow(() -> new AvatarNotFoundException("Аватар с ID " + entityID + " не найден."));
    }



    private void logWhenMethodInvoked(String methodName) {
        log.info("Method '{}' was invoked", methodName);
    }

    private void validateAvatar(MultipartFile avatarFile) {
        log.debug("Validating avatar file...");
        if (avatarFile == null || avatarFile.isEmpty()) {
            log.warn("Validation failed: the avatar file is empty or null.");
            throw new IllegalArgumentException("Файл аватара не может быть пустым.");
        }

        log.debug("Checking file extension...");
        String fileExtension = getExtension(avatarFile.getOriginalFilename());
        if (!extensions.contains(fileExtension)) {
            log.warn("Validation failed: file extension '{}' is not one of the allowed: {}", fileExtension, extensions);
            throw new IllegalArgumentException("Расширение файла должно быть одним из допустимых: " + extensions);
        }

        log.debug("Checking file content type...");
        if (!avatarFile.getContentType().startsWith("image/")) {
            log.warn("Validation failed: file content type '{}' is not an image.", avatarFile.getContentType());
            throw new IllegalArgumentException("Файл должен быть изображением.");
        }

        log.info("Avatar file validation successful.");
    }

    private Path saveImageToFileSystem(UUID entityID, MultipartFile avatarFile) throws IOException {
        log.debug("Saving image to the file system for entityID: {}", entityID);
        String fileName = entityID + "." + getExtension(avatarFile.getOriginalFilename());
        Path filePath = Paths.get(avatarsDir, fileName);

        try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, avatarFile.getBytes(), StandardOpenOption.CREATE);
            log.info("Image saved to the file system at: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to save image to the file system for entityID: {}", entityID, e);
            throw e;
        }

        return filePath;
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        log.debug("Original image size: width = {}, height = {}", originalWidth, originalHeight);

        float ratio = Math.min((float) targetWidth / originalWidth, (float) targetHeight / originalHeight);
        int newWidth = Math.round(originalWidth * ratio);
        int newHeight = Math.round(originalHeight * ratio);
        log.debug("Resizing image to width: {} and height: {} with aspect ratio maintained", newWidth, newHeight);

        Image resultingImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(resultingImage, 0, 0, null);
        g2d.dispose();

        log.info("Image resized with aspect ratio maintained successfully.");
        return outputImage;
    }


    private String getExtension(String fileName) {
        log.debug("Attempting to extract file extension from: {}", fileName);
        try {
            String extension = Optional.ofNullable(fileName)
                    .filter(f -> f.contains("."))
                    .map(f -> f.substring(fileName.lastIndexOf(".") + 1).toLowerCase())
                    .orElseThrow(() -> new IllegalArgumentException("Файл не содержит расширения."));
            log.info("File extension '{}' extracted successfully.", extension);
            return extension;
        } catch (Exception e) {
            log.error("Error determining file extension for file: {}", fileName, e);
            throw new IllegalArgumentException("Ошибка при определении расширения файла.", e);
        }
    }

}
