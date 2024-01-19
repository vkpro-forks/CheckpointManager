package ru.ac.checkpointmanager.service.avatar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.dto.avatar.AvatarImageDTO;
import ru.ac.checkpointmanager.exception.AvatarProcessingException;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.avatar.Avatar;
import ru.ac.checkpointmanager.model.avatar.AvatarProperties;
import ru.ac.checkpointmanager.repository.AvatarRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
class AvatarHelperImpl implements AvatarHelper {
    private final AvatarRepository repository;
    private final AvatarProperties avatarProperties;
    private final UserRepository userRepository;
    private final TerritoryRepository territoryRepository;

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
     * Получает существующий аватар пользователя или создает новый, если он не существует.
     *
     * @param userId Идентификатор пользователя.
     * @return Найденный или созданный объект Avatar.
     */
    @Override
    public Avatar getOrCreateAvatar(UUID userId) {
        return repository.findByUserId(userId).orElse(new Avatar());
    }

    @Override
    public Avatar getOrCreateAvatarByTerritory(UUID territoryId) {
        return repository.findByTerritoryId(territoryId).orElse(new Avatar());
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
     * Обрабатывает изображение аватара, проверяя его формат,
     * и устанавливает обработанное изображение в объект Avatar.
     *
     * @param avatar     Объект Avatar для установки изображения.
     * @param avatarFile Мультипарт-файл, содержащий изображение аватара.
     * @throws AvatarProcessingException если возникла проблема с обработкой изображения
     */
    @Override
    public void processAndSetAvatarImage(Avatar avatar, MultipartFile avatarFile) {
        // Проверяем размер файла
        try {
            // Читаем изображение из файла
            BufferedImage image = ImageIO.read(avatarFile.getInputStream());
            //TODO мы уже проверили что файл не null, контент там точно есть
            // Проверяем и обрабатываем разрешение изображения
            BufferedImage processedImage = processImage(image);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(processedImage, "png", baos);
            avatar.setPreview(baos.toByteArray());
            log.debug("Avatar image processed and set for entityID: {}", avatar.getId());
        } catch (IOException e) {
            log.error(ExceptionUtils.AVATAR_PROCESSING_ERROR.formatted(e.getMessage()));
            throw new AvatarProcessingException(ExceptionUtils.AVATAR_PROCESSING_ERROR.formatted(e.getMessage())); //TODO Лёш, жду от тебя тесты)))
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
        int maxWidth = avatarProperties.getMaxWidth();
        int maxHeight = avatarProperties.getMaxHeight();
        if (originalImage.getWidth() > maxWidth
                || originalImage.getHeight() > maxHeight) {
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
     */
    @Override
    public AvatarImageDTO createAvatarImageDTO(Avatar avatar) {
        byte[] imageData = avatar.getPreview(); //TODO мы же достали из БД, там всё хорошее должно быть

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

    /**
     * Обновляет аватар для заданной территории. Если территория не найдена, выбрасывает исключение.
     *
     * @param territoryId Идентификатор территорий, для которой нужно обновить аватар.
     * @param avatar      Объект Avatar, который будет установлен как аватар пользователя.
     * @throws TerritoryNotFoundException если пользователь с данным идентификатором не найден.
     */
    @Override
    public void updateTerritoryAvatar(UUID territoryId, Avatar avatar) {
        log.debug("Attempting to update avatar for user ID: {}", territoryId);
        Territory territory = territoryRepository.findById(territoryId)
                .orElseThrow(() -> {
                    return new TerritoryNotFoundException("This check should be not here");//FIXME это должно перед обработкой картинки
                });
        territory.setAvatar(avatar);
        territoryRepository.save(territory);
        log.info("Avatar updated successfully for user ID: {}", territoryId);
    }
}
