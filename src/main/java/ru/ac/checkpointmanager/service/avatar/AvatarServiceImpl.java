package ru.ac.checkpointmanager.service.avatar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.exception.AvatarNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.model.Avatar;
import ru.ac.checkpointmanager.model.AvatarProperties;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.repository.AvatarRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.utils.Mapper;

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

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AvatarServiceImpl implements AvatarService {

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
     * Загружает и сохраняет аватар пользователя. Если для данного пользователя уже существует аватар,
     * он будет обновлен новым изображением. В противном случае будет создан новый аватар.
     *
     * @param entityID    идентификатор пользователя, для которого загружается аватар.
     * @param avatarFile  файл аватара, который нужно загрузить.
     * @return            объект Avatar, представляющий загруженный или обновленный аватар.
     * @throws IOException если происходит ошибка ввода-вывода при обработке файла аватара.
     */
    @Override
    public Avatar uploadAvatar(UUID entityID, MultipartFile avatarFile) throws IOException {
        log.info("Method uploadAvatar invoked for entityID: {}", entityID);

        validateAvatar(avatarFile);
        log.debug("Avatar file validated successfully.");

        Avatar avatar = repository.findByUserId(entityID)
                .orElse(new Avatar());

        //смотрим, является ли аватар новым
        boolean isNewAvatar = avatar.getId() == null;
        log.debug("Avatar object {} for entityID: {}", isNewAvatar ? "created" : "found", entityID);


        // Создаем новый объект Avatar
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
        BufferedImage processedImage = (image.getWidth() > maxWidth || image.getHeight() > maxHeight)
                ? resizeImage(image, maxWidth, maxHeight)
                : image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(processedImage, "png", baos);
        avatar.setPreview(baos.toByteArray());
        log.debug("Avatar image processed and set for entityID: {}", entityID);

        //Сохраняем изобра
        log.info("Avatar object saved/updated in the repository for entityID: {}", entityID);
        avatar = repository.save(avatar);

        User user = userRepository.findById(entityID).orElseThrow(() ->
                new UserNotFoundException("Пользователь с ID " + entityID + " не найден."));
        user.setAvatar(avatar);
        userRepository.save(user);

        log.info("Avatar ID updated for user {}", entityID);
        return avatar;
    }


    /**
     * Возвращает аватар для указанного идентификатора сущности.
     *
     * @param userId Уникальный идентификатор сущности, аватар которой нужно получить.
     * @return Объект Avatar, соответствующий указанному идентификатору сущности.
     * @throws IOException если возникают проблемы при чтении файла аватара.
     */
    @Override
    public byte[] getAvatarByUserId(UUID userId) {
        log.debug("Fetching avatar for user ID: {}", userId);

        UUID avatarId = userRepository.findAvatarIdByUserId(userId)
                .orElseThrow(() -> new AvatarNotFoundException("Avatar not found for user ID: " + userId));

        Avatar avatar = repository.findById(avatarId)
                .orElseThrow(() -> new AvatarNotFoundException("Avatar with ID " + avatarId + " not found"));


        byte[] imageData = avatar.getPreview();
        if (imageData == null || imageData.length == 0) {
            log.warn("No image data available for avatar of user ID: {}", userId);
            throw new AvatarNotFoundException("Изображение аватара для пользователя с ID " + userId + " отсутствует.");
        }

        log.debug("Avatar image fetched successfully for user ID: {}", userId);
        return imageData;
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
        return findAvatarOrThrow(entityID);
    }

    @Override
    public Avatar findAvatarOrThrow(UUID avatarId) {
        log.debug("Searching for avatar with ID: {}", avatarId);
        return repository.findById(avatarId).orElseThrow(
                () -> new AvatarNotFoundException("Аватар с ID " + avatarId + " не найден."));
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
