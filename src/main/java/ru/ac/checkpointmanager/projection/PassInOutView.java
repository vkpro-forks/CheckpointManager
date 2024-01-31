package ru.ac.checkpointmanager.projection;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.model.passes.PassTimeType;

import java.time.LocalDateTime;
import java.util.UUID;

public interface PassInOutView {

    String getDtype();
    String getPass_comment();
    String getTerr_name();
    @JsonProperty("timeType")
    String getPass_time_type();
    @JsonProperty("status")
    String getPass_status();
    String getCar_number();
    String getCar_brand();
    String getVisitor();
    LocalDateTime getIn_time();
    LocalDateTime getOut_time();
    UUID getPass_id();

    @JsonProperty("statusDescription")
    default String getPassStatusDescription() {
        return PassStatus.fromString(getPass_status()).getDescription();
    }
    @JsonProperty("timeTypeDescription")
    default String getPassTimeTypeDescription() {
        return PassTimeType.fromString(getPass_time_type()).getDescription();
    }
}