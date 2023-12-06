package ru.ac.checkpointmanager.projection;

import java.time.LocalDateTime;
import java.util.UUID;

public interface PassInOutViewProjection {

    String getDtype();
    String getComment();
    String getName();
    String getType_time();
    String getStatus();
    String getCar();
    String getVisitor();
    LocalDateTime getIn_time();
    LocalDateTime getOut_time();
    UUID getPass_id();
}