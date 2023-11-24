package ru.ac.checkpointmanager.service.avatar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.dto.AvatarImageDTO;
import ru.ac.checkpointmanager.exception.AvatarLoadingException;
import ru.ac.checkpointmanager.dto.AvatarDTO;
import ru.ac.checkpointmanager.exception.AvatarNotFoundException;
import ru.ac.checkpointmanager.exception.AvatarProcessingException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.mapper.AvatarMapper;
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

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AvatarServiceImpl implements AvatarService {

    public static final String AVATAR_NOT_FOUND_LOG = "[Avatar with id: {}] not found";
    public static final String AVATAR_NOT_FOUND_MSG = "Avatar with id: %s not found";
    private final AvatarRepository repository;
    private final AvatarProperties avatarProperties;
    private final UserRepository userRepository;
    private final AvatarMapper avatarMapper;


    @Value("${avatars.extensions}")
    private String extensions;

    @Value("${avatars.image.max-width}")
    private int maxWidth;

    @Value("${avatars.image.max-height}")
    private int maxHeight;

    @Value("${avatars.max-size}")
    private String maxFileSize;

    /**
     * Загружает и сохраняет аватар пользователя. Если для данного пользователя уже существует аватар,
     * он будет обновлен новым изображением. В противном случае будет создан новый аватар.
     *
     * @param userId     идентификатор пользователя, для которого загружается аватар.
     * @param avatarFile файл аватара, который нужно загрузить.
     * @return объект Avatar, представляющий загруженный или обновленный аватар.
     * @throws IOException если происходит ошибка ввода-вывода при обработке файла аватара.
     */
    @Override
    public AvatarDTO uploadAvatar(UUID userId, MultipartFile avatarFile) {
        log.info("Method uploadAvatar invoked for entityId: {}", userId);

        validateAvatar(avatarFile);
        Avatar avatar = getOrCreateAvatar(userId);
        configureAvatar(avatar, avatarFile);
        processAndSetAvatarImage(avatar, avatarFile);
        avatar = saveAvatar(avatar);
        updateUserAvatar(userId, avatar);

        log.info("Avatar ID updated for user {}", userId);
        return avatarMapper.toAvatarDTO(avatar);
    }


    /**
     * Возвращает аватар для указанного идентификатора сущности.
     *
     * @param userId Уникальный идентификатор сущности, аватар которой нужно получить.
     * @return Объект AvatarImageDTO, соответствующий указанному идентификатору сущности.
     */
    @Override
    public AvatarImageDTO getAvatarByUserId(UUID userId) {
        log.debug("Fetching avatar for user ID: {}", userId);
        UUID avatarId = userRepository.findAvatarIdByUserId(userId);
        if (avatarId == null) {
            log.warn("[User with id: {}] doesn't have avatar", userId);
            throw new AvatarNotFoundException("User with id: %s doesn't have avatar".formatted(userId));
        }
        log.debug("Searching [avatar with id: {}]", avatarId);
        Avatar avatar = repository.findById(avatarId)
                .orElseThrow(() -> {
                    log.warn(AVATAR_NOT_FOUND_LOG, avatarId);
                    return new AvatarNotFoundException(AVATAR_NOT_FOUND_MSG.formatted(avatarId));
                });

        byte[] imageData = avatar.getPreview();
        if (imageData == null || imageData.length == 0) {
            log.warn("Avatar image data is empty for user ID: {}", userId);
            throw new AvatarLoadingException("Avatar image data is empty");
        }

        String mimeType = avatar.getMediaType();
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
            return new AvatarImageDTO(
                    avatarId,
                    mimeType,
                    imageData,
                    null,
                    avatar.getFileSize()
            );
    }


    /**
     * Удаляет аватар пользователя, если он существует, и возвращает удаленный аватар.
     * Если аватар для указанного идентификатора сущности не найден, ничего не делает и возвращает null.
     *
     * @param entityID Уникальный идентификатор сущности, аватар которой нужно удалить.
     * @return Удаленный объект Avatar или null, если аватар не был найден.
     */
    public Avatar deleteAvatarIfExists(UUID entityID) {
        log.debug("Attempting to delete avatar for entity ID: {}", entityID);
        return findAvatarById(entityID);
    }

    @Override
    public AvatarImageDTO getAvatarImageByAvatarId(UUID avatarId) {
        log.debug("Fetching avatar image for avatar ID: {}", avatarId);
        Avatar avatar = repository.findById(avatarId)
                .orElseThrow(() -> {
                    log.warn(AVATAR_NOT_FOUND_LOG, avatarId);
                    return new AvatarNotFoundException(AVATAR_NOT_FOUND_MSG.formatted(avatarId));
                });

        byte[] imageData = avatar.getPreview();
        if (imageData == null || imageData.length == 0) {
            log.warn("Avatar image data is empty for avatar ID: {}", avatarId);
            throw new AvatarLoadingException("Avatar image data is empty for avatar ID: " + avatarId);
        }

        String mimeType = avatar.getMediaType();
        mimeType = (mimeType != null) ? mimeType : "application/octet-stream";


        return new AvatarImageDTO(
                avatarId,
                mimeType,
                imageData,
                null,
                avatar.getFileSize()
        );
    }

    @Override
    public Avatar findAvatarById(UUID avatarId) {
        log.debug("Searching for avatar with ID: {}", avatarId);
        return repository.findById(avatarId).orElseThrow(() -> {
            log.warn(AVATAR_NOT_FOUND_LOG, avatarId);
            return new AvatarNotFoundException(AVATAR_NOT_FOUND_MSG.formatted(avatarId));
        });
    }


    private Avatar getOrCreateAvatar(UUID entityId) {
        return repository.findByUserId(entityId).orElse(new Avatar());
    }

    private void configureAvatar(Avatar avatar, MultipartFile avatarFile) {
        avatar.setFileSize(avatarFile.getSize());
        avatar.setMediaType(avatarFile.getContentType());
    }

    private void processAndSetAvatarImage(Avatar avatar, MultipartFile avatarFile) {
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

    private BufferedImage processImage(BufferedImage originalImage) {
        // Проверяем разрешение изображения
        if (originalImage.getWidth() > maxWidth || originalImage.getHeight() > maxHeight) {
            return resizeImage(originalImage, maxWidth, maxHeight);
        } else {
            return originalImage;
        }
    }

    private Avatar saveAvatar(Avatar avatar) {
        return repository.save(avatar);
    }

    private void updateUserAvatar(UUID entityId, Avatar avatar) {
        User user = userRepository.findById(entityId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + entityId + " не найден."));
        user.setAvatar(avatar);
        userRepository.save(user);
    }

    /**
     * Проверяет файл аватара на предмет корректности: файл не должен быть пустым или null,
     * должен иметь допустимое расширение файла и тип содержимого, начинающийся с "image/".
     * В случае обнаружения некорректности файла выбрасывает IllegalArgumentException.
     *
     * @param avatarFile Мультипарт-файл, представляющий аватар, который нужно проверить.
     * @throws IllegalArgumentException если файл аватара не проходит валидацию.
     */
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


    /**
     * Изменяет размер переданного изображения, сохраняя его пропорции.
     * Размер изменяется в соответствии с заданными целевыми шириной и высотой.
     *
     * @param originalImage Исходное изображение для изменения размера.
     * @param targetWidth   Целевая ширина для исходного изображения.
     * @param targetHeight  Целевая высота для исходного изображения.
     * @return BufferedImage Измененное изображение с новыми размерами.
     */
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

    /**
     * Возвращает расширение файла из полного имени файла.
     * Расширение извлекается как подстрока после последней точки в имени файла.
     *
     * @param fileName Имя файла для извлечения расширения.
     * @return String Расширение файла.
     * @throws IllegalArgumentException если имя файла не содержит расширения.
     */
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
