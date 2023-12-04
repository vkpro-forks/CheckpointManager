package ru.ac.checkpointmanager.model.avatar;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "avatars")
@Component
public class AvatarProperties {

    private String dir;
    private List<String> extensions;
    private DataSize maxSize;
    private int maxWidth;
    private int maxHeight;
    private String contentType;

    public void setExtensions(List<String> extensions) {
        this.extensions = extensions;
    }

    public long getMaxSizeInBytes() {
        return maxSize.toBytes();
    }

}

