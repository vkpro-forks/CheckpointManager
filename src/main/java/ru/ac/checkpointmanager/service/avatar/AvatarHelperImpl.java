package ru.ac.checkpointmanager.service.avatar;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.dto.AvatarImageDTO;
import ru.ac.checkpointmanager.exception.AvatarLoadingException;
import ru.ac.checkpointmanager.exception.AvatarProcessingException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.model.Avatar;
import ru.ac.checkpointmanager.model.AvatarProperties;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.repository.AvatarRepository;
import ru.ac.checkpointmanager.repository.UserRepository;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
class AvatarHelperImpl implements AvatarHelper {

    private final AvatarRepository repository;
    private final AvatarProperties avatarProperties;
    private final UserRepository userRepository;

    @Value("${avatars.extensions}")
    private String extensions;

    @Value("${avatars.image.max-width}")
    private int maxWidth;

    @Value("${avatars.image.max-height}")
    private int maxHeight;

    @Value("${avatars.max-size}")
    private String maxFileSize;


    /**
     * Возвращает расширение файла из полного имени файла.
     * Расширение извлекается как подстрока после последней точки в имени файла.
     *
     * @param fileName Имя файла для извлечения расширения.
     * @return String Расширение файла.
     * @throws IllegalArgumentException если имя файла не содержит расширения.
     */
    @Override
    public String getExtension(String fileName) {
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

    /**
     * Изменяет размер переданного изображения, сохраняя его пропорции.
     * Размер изменяется в соответствии с заданными целевыми шириной и высотой.
     *
     * @param originalImage Исходное изображение для изменения размера.
     * @param targetWidth   Целевая ширина для исходного изображения.
     * @param targetHeight  Целевая высота для исходного изображения.
     * @return BufferedImage Измененное изображение с новыми размерами.
     */
    @Override
    public BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
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

    /**
     * Проверяет файл аватара на предмет корректности: файл не должен быть пустым или null,
     * должен иметь допустимое расширение файла и тип содержимого, начинающийся с "image/".
     * В случае обнаружения некорректности файла выбрасывает IllegalArgumentException.
     *
     * @param avatarFile Мультипарт-файл, представляющий аватар, который нужно проверить.
     * @throws IllegalArgumentException если файл аватара не проходит валидацию.
     */
    @Override
    public void validateAvatar(MultipartFile avatarFile) {
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

    @Override
    public Avatar getOrCreateAvatar(UUID entityId) {
        return repository.findByUserId(entityId).orElse(new Avatar());
    }

    @Override
    public void configureAvatar(Avatar avatar, MultipartFile avatarFile) {
        avatar.setFileSize(avatarFile.getSize());
        avatar.setMediaType(avatarFile.getContentType());
    }

    @Override
    public void processAndSetAvatarImage(Avatar avatar, MultipartFile avatarFile) {
        // Проверяем размер файла
        try {
            if (avatarFile.getSize() > avatarProperties.getMaxSizeInBytes()) {
                log.warn("File size {} exceeds the maximum allowed size of {}", avatarFile.getSize(), maxFileSize);
                throw new IOException("Слишком большой файл");
            }

            // Читаем изображение из файла
            BufferedImage image = ImageIO.read(avatarFile.getInputStream());
            if (image == null) {
                log.warn("The file uploaded for entityID: {} is not an image.", avatar.getId());
                throw new IllegalArgumentException("Файл не является изображением.");
            }

            // Проверяем и обрабатываем разрешение изображения
            BufferedImage processedImage = processImage(image);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(processedImage, "png", baos);
            avatar.setPreview(baos.toByteArray());
            log.debug("Avatar image processed and set for entityID: {}", avatar.getId());
        } catch (IOException e) {
            throw new AvatarProcessingException("Error processing avatar", e); //нужно обратить внимание
        }


    }

    @Override
    public BufferedImage processImage(BufferedImage originalImage) {
        // Проверяем разрешение изображения
        if (originalImage.getWidth() > maxWidth || originalImage.getHeight() > maxHeight) {
            return resizeImage(originalImage, maxWidth, maxHeight);
        } else {
            return originalImage;
        }
    }

    @Override
    public Avatar saveAvatar(Avatar avatar) {
        return repository.save(avatar);
    }

    @Override
    public void updateUserAvatar(UUID entityId, Avatar avatar) {
        User user = userRepository.findById(entityId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + entityId + " не найден."));
        user.setAvatar(avatar);
        userRepository.save(user);
    }

    @Override
    public AvatarImageDTO createAvatarImageDTO(Avatar avatar) {
        byte[] imageData = avatar.getPreview();
        if (imageData == null || imageData.length == 0) {
            log.warn("Avatar image data is empty for avatar ID: {}", avatar.getId());
            throw new AvatarLoadingException("Avatar image data is empty for avatar ID: " + avatar.getId());
        }

        String mimeType = avatar.getMediaType();
        mimeType = (mimeType != null) ? mimeType : "application/octet-stream";

        return new AvatarImageDTO(
                avatar.getId(),
                mimeType,
                imageData,
                null,
                avatar.getFileSize()
        );
    }
}
