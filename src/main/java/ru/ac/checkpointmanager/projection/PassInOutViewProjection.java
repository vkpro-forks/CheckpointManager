package ru.ac.checkpointmanager.projection;

import java.time.LocalDateTime;
import java.util.UUID;

public interface PassInOutViewProjection {

    String getDtype();
    String getPass_comment();
    String getTerr_name();
    String getPass_time_type();
    String getPass_status();
    String getCar();
    String getVisitor();
    LocalDateTime getIn_time();
    LocalDateTime getOut_time();
    UUID getPass_id();
}