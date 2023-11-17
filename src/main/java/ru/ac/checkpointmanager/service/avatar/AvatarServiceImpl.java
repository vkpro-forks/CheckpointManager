package ru.ac.checkpointmanager.service.avatar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.exception.AvatarNotFoundException;
import ru.ac.checkpointmanager.model.Avatar;
import ru.ac.checkpointmanager.model.AvatarProperties;
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
    private final Mapper mapper;

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

    /**
     * Загружает и сохраняет аватар пользователя на основе предоставленного файла изображения.
     * В процессе загрузки проверяется размер файла и разрешение изображения.
     * Если изображение превышает установленные ограничения, оно автоматически изменяется до допустимых размеров.
     *
     * @param entityID   Уникальный идентификатор сущности, для которой загружается аватар.
     * @param avatarFile Файл изображения, который будет использоваться как аватар.
     * @return Объект Avatar, содержащий данные о загруженном изображении.
     * @throws IOException              Если возникает ошибка при чтении файла изображения.
     * @throws IllegalArgumentException Если файл не является изображением или изображение не соответствует требованиям.
     */
    @Override
    public Avatar uploadAvatar(UUID entityID, MultipartFile avatarFile) throws IOException {
        log.info("Method uploadAvatar invoked for entityID: {}", entityID);

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


    /**
     * Возвращает аватар для указанного идентификатора сущности.
     *
     * @param userId Уникальный идентификатор сущности, аватар которой нужно получить.
     * @return Объект Avatar, соответствующий указанному идентификатору сущности.
     * @throws IOException если возникают проблемы при чтении файла аватара.
     */
    @Override
    public Avatar getAvatarByUserId(UUID userId) {
        log.debug("Fetching avatar for user ID: {}", userId);
        UUID avatarId = userRepository.findAvatarIdByUserId(userId);
        if (avatarId == null) {
            log.info("У пользователя с ID {} нет аватара.", userId);
            throw new AvatarNotFoundException("У пользователя с ID " + userId + " нет аватара.");
        }

        log.debug("Поиск аватара с ID {}", avatarId);
        return repository.findById(avatarId)
                .orElseThrow(() -> new AvatarNotFoundException("Аватар с ID " + avatarId + " не найден."));
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
     * Сохраняет изображение, полученное от пользователя, в файловую систему.
     * Имя файла генерируется путем объединения идентификатора сущности с оригинальным расширением файла.
     * Создает необходимые директории, если они еще не существуют.
     *
     * @param entityID   Идентификатор сущности, для которой сохраняется изображение.
     * @param avatarFile Мультипарт-файл, содержащий данные изображения.
     * @return Путь к файлу в файловой системе, где было сохранено изображение.
     * @throws IOException Если возникают проблемы при сохранении файла.
     */
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
