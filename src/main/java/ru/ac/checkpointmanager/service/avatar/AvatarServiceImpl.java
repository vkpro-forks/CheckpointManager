package ru.ac.checkpointmanager.service.avatar;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.exception.AvatarIsTooBigException;
import ru.ac.checkpointmanager.exception.AvatarNotFoundException;
import ru.ac.checkpointmanager.model.Avatar;
import ru.ac.checkpointmanager.repository.AvatarRepository;
import ru.ac.checkpointmanager.utils.MethodLog;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @Override
    public void uploadAvatar(UUID entityID, MultipartFile avatarFile) throws IOException {
        logWhenMethodInvoked(MethodLog.getMethodName());
        long imageSize = avatarFile.getSize();

        if (imageSize > (1024 * 5000)) {
            log.error("Image is too big for avatar. Size = {} MB", imageSize / 1024 / (double) 1000);
            throw new AvatarIsTooBigException("File size exceeds maximum permitted value of 5MB");
        }

        log.debug("Creating directory if absent, deleting image f already exists");
        Path filePath = Path.of(avatarsDir, entityID + "." +
                getExtension(avatarFile.getOriginalFilename()));
        Files.createDirectories(filePath.getParent());
        Files.deleteIfExists(filePath);
        try (
                InputStream is = avatarFile.getInputStream();
                OutputStream os = Files.newOutputStream(filePath, CREATE_NEW);
                BufferedInputStream bis = new BufferedInputStream(is, 1024);
                BufferedOutputStream bos = new BufferedOutputStream(os, 1024)
        ) {
            bis.transferTo(bos);
        }
        log.debug("Filling avatar object with values and saving in repository");
        Avatar avatar = new Avatar();
        avatar.setAvatarHolder(entityID);
        avatar.setFilePath(filePath.toString());
        avatar.setFileSize(avatarFile.getSize());
        avatar.setMediaType(avatarFile.getContentType());
        avatar.setPreview(generateImagePreview(filePath));
        repository.save(avatar);
    }

    @Override
    public void getAvatar(UUID entityID, HttpServletResponse response) throws IOException {
        logWhenMethodInvoked(MethodLog.getMethodName());
        Avatar avatar = findAvatarOrThrow(entityID);
        try (
                InputStream is = Files.newInputStream(Path.of(avatar.getFilePath()));
                BufferedInputStream bis = new BufferedInputStream(is, 1024);
                BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream(), 1024)
        ) {
            response.setContentType(avatar.getMediaType());
            response.setContentLength(avatar.getFileSize().intValue());
            response.setStatus(HttpServletResponse.SC_OK);
            bis.transferTo(bos);
        }
    }

    @Override
    public void deleteAvatar(UUID entityID) {
        logWhenMethodInvoked(MethodLog.getMethodName());
        Avatar avatar = findAvatarOrThrow(entityID);
        Path filePath = Path.of(avatarsDir, entityID + "." +
                getExtension(avatar.getFilePath()));
        try {
            Files.delete(filePath);
        } catch (IOException e) {
            log.error("I/O error occurred");
        }
        repository.delete(avatar);
    }

    @Override
    public Avatar findAvatarOrThrow(UUID entityID) {
        Optional<Avatar> avatar = repository.findById(entityID);
        if (avatar.isPresent()) {
            return avatar.get();
        }
        log.info("Entity with UUID = {} has no avatar", entityID);
        throw new AvatarNotFoundException("Avatar for this entity is not uploaded yet");
    }

    /**
     * Generate byte array of compressed avatar image of given file path.
     * @param filePath path to image file
     * @return byte array of compressed avatar
     * @throws IOException if file is not found
     */
    private byte[] generateImagePreview(Path filePath) throws IOException {
        logWhenMethodInvoked(MethodLog.getMethodName());
        try (
                InputStream is = Files.newInputStream(filePath);
                BufferedInputStream bis = new BufferedInputStream(is, 1024);
                ByteArrayOutputStream baos = new ByteArrayOutputStream()
        ) {
            BufferedImage image = ImageIO.read(bis);

            log.debug("Creating avatar preview and return result of it as byte array");
            int height = image.getHeight() / (image.getWidth() / 100);
            BufferedImage preview = new BufferedImage(100, height, image.getType());
            Graphics2D graphics = preview.createGraphics();
            graphics.drawImage(image, 0, 0, 100, height, null);
            graphics.dispose();
            ImageIO.write(preview, getExtension(filePath.getFileName().toString()), baos);
            return baos.toByteArray();
        }
    }

    /**
     * Utility method which is responsible for extracting extension of the file name
     * @param fileName file name for getting extension
     * @return String that represents extension of file
     */
    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private void logWhenMethodInvoked(String methodName) {
        log.info("Method '{}' was invoked", methodName);
    }
}
