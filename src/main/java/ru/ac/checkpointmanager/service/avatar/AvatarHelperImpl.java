package ru.ac.checkpointmanager.service.avatar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.dto.avatar.AvatarImageDTO;
import ru.ac.checkpointmanager.exception.AvatarLoadingException;
import ru.ac.checkpointmanager.exception.AvatarProcessingException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.avatar.Avatar;
import ru.ac.checkpointmanager.model.avatar.AvatarProperties;
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
     * Извлекает расширение файла из его полного имени.
     * Расширение определяется как подстрока после последней точки в имени файла.
     *
     * @param fileName Имя файла, для которого необходимо извлечь расширение.
     * @return Расширение файла в нижнем регистре.
     * @throws IllegalArgumentException если имя файла не содержит точку или расширение.
     */
    @Override
    public String getExtension(String fileName) {
        log.debug("Attempting to extract file extension from: {}", fileName);
        try {
            String extension = Optional.ofNullable(fileName)
                    .filter(f -> f.contains("."))
                    .map(f -> f.substring(fileName.lastIndexOf(".") + 1).toLowerCase())
                    .orElseThrow(() -> new IllegalArgumentException("The file does not contain an extension."));
            log.info("File extension '{}' extracted successfully.", extension);
            return extension;
        } catch (Exception e) {
            log.warn("Error determining file extension for file: {}", fileName, e);
            throw new IllegalArgumentException("There was an error determining the file extension.", e);
        }
    }

    /**
     * Изменяет размер переданного изображения с сохранением пропорций.
     * Размеры изменяются в соответствии с заданными целевыми шириной и высотой.
     *
     * @param originalImage Исходное изображение для изменения размера.
     * @param targetWidth   Целевая ширина изображения.
     * @param targetHeight  Целевая высота изображения.
     * @return Изменённое изображение с новыми размерами.
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
     * Проверяет файл аватара на соответствие определенным требованиям:
     * файл не должен быть пустым или null, должен иметь допустимое расширение
     * и тип содержимого, начинающийся с "image/".
     *
     * @param avatarFile Мультипарт-файл, представляющий аватар.
     * @throws IllegalArgumentException если файл не проходит проверку.
     */
    @Override
    public void validateAvatar(MultipartFile avatarFile) {//TODO уйдет в валидацию
        log.debug("Validating avatar file...");
        if (avatarFile == null || avatarFile.isEmpty()) {
            log.warn("Validation failed: the avatar file is empty or null.");
            throw new IllegalArgumentException("Avatar file cannot be empty.");
        }

        log.debug("Checking file extension...");
        String fileExtension = getExtension(avatarFile.getOriginalFilename());
        if (!extensions.contains(fileExtension)) {
            log.warn("Validation failed: file extension '{}' is not one of the allowed: {}", fileExtension, extensions);
            throw new IllegalArgumentException("The file extension must be one of the valid ones: " + extensions);
        }

        log.debug("Checking file content type...");
        if (!avatarFile.getContentType().startsWith("image/")) {
            log.warn("Validation failed: file content type '{}' is not an image.", avatarFile.getContentType());
            throw new IllegalArgumentException("The file must be an image.");
        }
        log.info("Avatar file validation successful.");
    }

    /**
     * Получает существующий аватар пользователя или создает новый, если он не существует.
     *
     * @param userId Идентификатор пользователя.
     * @return Найденный или созданный объект Avatar.
     */
    @Override
    public Avatar getOrCreateAvatar(UUID userId) {
        return repository.findByUserId(userId).orElse(new Avatar());
    }

    /**
     * Конфигурирует объект Avatar, устанавливая размер файла и тип медиа.
     *
     * @param avatar     Объект Avatar для конфигурации.
     * @param avatarFile Мультипарт-файл, содержащий данные аватара.
     */
    @Override
    public void configureAvatar(Avatar avatar, MultipartFile avatarFile) {
        avatar.setFileSize(avatarFile.getSize());
        avatar.setMediaType(avatarFile.getContentType());
    }

    /**
     * Обрабатывает изображение аватара, проверяя его размер и формат,
     * и устанавливает обработанное изображение в объект Avatar.
     *
     * @param avatar     Объект Avatar для установки изображения.
     * @param avatarFile Мультипарт-файл, содержащий изображение аватара.
     * @throws IllegalArgumentException если файл не является изображением.
     */
    @Override
    public void processAndSetAvatarImage(Avatar avatar, MultipartFile avatarFile) {
        // Проверяем размер файла
        try {
            if (avatarFile.getSize() > avatarProperties.getMaxSizeInBytes()) {
                log.warn("File size {} exceeds the maximum allowed size of {}", avatarFile.getSize(), maxFileSize);
                throw new IOException("File too big");
            }

            // Читаем изображение из файла
            BufferedImage image = ImageIO.read(avatarFile.getInputStream());
            if (image == null) {
                log.warn("The file uploaded for entityID: {} is not an image.", avatar.getId());
                throw new IllegalArgumentException("The file is not an image.");
            }

            // Проверяем и обрабатываем разрешение изображения
            BufferedImage processedImage = processImage(image);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(processedImage, "png", baos);
            avatar.setPreview(baos.toByteArray());
            log.debug("Avatar image processed and set for entityID: {}", avatar.getId());
        } catch (IOException e) {
            throw new AvatarProcessingException("Error processing avatar", e);  //FIXME we need to do smth to avoid this situation
        }
    }

    /**
     * Обрабатывает изображение, изменяя его размер в соответствии с максимально допустимыми размерами,
     * если необходимо, сохраняя при этом пропорции изображения.
     *
     * @param originalImage Оригинальное изображение для обработки.
     * @return Обработанное изображение, соответствующее максимально допустимым размерам.
     */
    @Override
    public BufferedImage processImage(BufferedImage originalImage) {
        log.debug("Processing image with original size: width = {}, height = {}", originalImage.getWidth(), originalImage.getHeight());
        // Проверяем разрешение изображения
        if (originalImage.getWidth() > maxWidth || originalImage.getHeight() > maxHeight) {
            log.info("Image size exceeds max dimensions ({}x{}), resizing required.", maxWidth, maxHeight);
            BufferedImage resizedImage = resizeImage(originalImage, maxWidth, maxHeight);
            log.debug("Image resized to: width = {}, height = {}", resizedImage.getWidth(), resizedImage.getHeight());
            return resizedImage;
        } else {
            log.debug("Image size is within max dimensions, no resizing required.");
            return originalImage;
        }
    }

    /**
     * Сохраняет объект Avatar в репозитории.
     *
     * @param avatar Объект Avatar для сохранения.
     * @return Сохраненный объект Avatar.
     */
    @Override
    public Avatar saveAvatar(Avatar avatar) {
        return repository.save(avatar);
    }

    /**
     * Обновляет аватар для заданного пользователя. Если пользователь не найден, выбрасывает исключение.
     *
     * @param userId Идентификатор пользователя, для которого нужно обновить аватар.
     * @param avatar Объект Avatar, который будет установлен как аватар пользователя.
     * @throws UserNotFoundException если пользователь с данным идентификатором не найден.
     */
    @Override
    public void updateUserAvatar(UUID userId, Avatar avatar) {
        log.debug("Attempting to update avatar for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    return new UserNotFoundException("This check should be not here");//FIXME это должно перед обработкой картинки
                });
        user.setAvatar(avatar);
        userRepository.save(user);
        log.info("Avatar updated successfully for user ID: {}", userId);
    }

    /**
     * Создает DTO для изображения аватара. Если данные изображения пусты, выбрасывает исключение.
     *
     * @param avatar Объект Avatar, содержащий данные изображения.
     * @return AvatarImageDTO, содержащий данные изображения аватара.
     * @throws AvatarLoadingException если данные изображения аватара пусты.
     */
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
