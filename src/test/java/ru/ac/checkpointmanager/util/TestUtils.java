package ru.ac.checkpointmanager.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.checkpoints.CheckpointType;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.passes.PassTypeTime;
import ru.ac.checkpointmanager.model.checkpoints.CheckpointType;
import ru.ac.checkpointmanager.model.enums.Direction;

import java.time.LocalDateTime;
import java.util.UUID;

public class TestUtils {

    public static final Long CAR_BRAND_ID = 1000L;

    public static final String CAR_BRAND_ID_STR = CAR_BRAND_ID.toString();

    public static final UUID USER_ID = UUID.randomUUID();

    public static final UUID PASS_ID = UUID.randomUUID();

    public static final UUID CHECKPOINT_ID = UUID.randomUUID();

    public static final String CHECKPOINT_NAME = "ch_name";

    public static final UUID TERR_ID = UUID.randomUUID();

    public static final String TERR_NAME = "Territory";

    public static final String JSON_ERROR_CODE = "$.errorCode";

    public static final String JSON_TIMESTAMP = "$.timestamp";

    public static final String JSON_VIOLATIONS_FIELD = "$.violations[0].fieldName";
    public static final String JSON_TITLE = "$.title";


    public static CarBrand getCarBrand() {
        CarBrand carBrand = new CarBrand();
        carBrand.setId(CAR_BRAND_ID);
        carBrand.setBrand("Buhanka");
        return carBrand;
    }

    public static CrossingDTO getCrossingDTO() {
        return new CrossingDTO(
                PASS_ID,
                CHECKPOINT_ID,
                LocalDateTime.now(),
                Direction.IN
        );
    }

    public static TerritoryDTO getTerritoryDTO() {
        return new TerritoryDTO(
                TERR_ID,
                TERR_NAME,
                "note"
        );
    }

    public static CheckpointDTO getCheckPointDTO() {
        return new CheckpointDTO(
                CHECKPOINT_ID,
                CHECKPOINT_NAME,
                CheckpointType.UNIVERSAL,
                "note", getTerritoryDTO()
        );
    }

    public static String jsonStringFromObject(Object object) throws JsonProcessingException {
        ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
        return objectMapper.writeValueAsString(object);
    }

    private TestUtils() {
    }
}
