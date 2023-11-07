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

    @Value("${avatars.dir.path}")
    private String avatarsDir;

    @Value("${avatars.extensions}")
    private String extensions;

    @Value("${avatars.image.max-width}")
    private int maxWidth;

    @Value("${avatars.image.max-height}")
    private int maxHeight;

    @Value("${avatars.max-size}")
    private long maxFileSize;

    @Override
    public void uploadAvatar(UUID entityID, MultipartFile avatarFile) throws IOException {
        logWhenMethodInvoked(MethodLog.getMethodName());
        validateAvatar(avatarFile);

        // Создаем новый объект Avatar
        Avatar avatar = new Avatar();
        avatar.setFileSize(avatarFile.getSize());
        avatar.setMediaType(avatarFile.getContentType());

        // Проверяем размер файла
        if (avatarFile.getSize() > maxFileSize) {
            throw new IOException("Слишком большой файл"); // Или другое исключение, которое ты обрабатываешь
        }

        // Читаем изображение из файла
        BufferedImage image = ImageIO.read(avatarFile.getInputStream());
        if (image == null) {
            throw new IllegalArgumentException("Файл не является изображением.");
        }

        // Проверяем разрешение изображения
        if (image.getWidth() > maxWidth || image.getHeight() > maxHeight) {
            // Сохраняем большое изображение в файловой системе
            log.debug("Saving large image to file system");
            Path filePath = saveImageToFileSystem(entityID, avatarFile);
            avatar.setFilePath(filePath.toString());

            // Создаем уменьшенную версию изображения для БД
            BufferedImage resizedImage = resizeImage(image, maxWidth, maxHeight);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "png", baos);
            avatar.setPreview(baos.toByteArray());
        } else {
            // Сохраняем маленькое изображение напрямую в БД
            log.debug("Storing small image as preview in database");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            avatar.setPreview(baos.toByteArray());
        }

        log.debug("Saving avatar object in repository");
        repository.save(avatar);
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(resultingImage, 0, 0, null);
        g2d.dispose();
        return outputImage;
    }

    @Override
    public void getAvatar(UUID entityID, HttpServletResponse response) throws IOException {

    }

    @Override
    public Avatar deleteAvatarIfExists(UUID entityID) {
        return null;
    }

    @Override
    public Avatar findAvatarOrThrow(UUID entityID) {
        return null;
    }

    private void logWhenMethodInvoked(String methodName) {
        log.info("Method '{}' was invoked", methodName);
    }

    private void validateAvatar(MultipartFile avatarFile) {
        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new IllegalArgumentException("Файл аватара не может быть пустым.");
        }

        String fileExtension = getExtension(avatarFile.getOriginalFilename());
        if (!extensions.contains(fileExtension)) {
            throw new IllegalArgumentException("Расширение файла должно быть одним из допустимых: " + extensions);
        }

        if (!avatarFile.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Файл должен быть изображением.");
        }
    }

    private Path saveImageToFileSystem(UUID entityID, MultipartFile avatarFile) throws IOException {
        String fileName = entityID + "." + getExtension(avatarFile.getOriginalFilename());
        Path filePath = Paths.get(avatarsDir, fileName);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, avatarFile.getBytes(), StandardOpenOption.CREATE);
        return filePath;
    }

    private String getExtension(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка при определении расширения файла.");
        }
    }

}
