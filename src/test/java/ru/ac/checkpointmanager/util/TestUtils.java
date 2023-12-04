package ru.ac.checkpointmanager.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.dto.CheckpointDTO;
import ru.ac.checkpointmanager.dto.CrossingDTO;
import ru.ac.checkpointmanager.dto.PhoneDTO;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.dto.passes.PassUpdateDTO;
import ru.ac.checkpointmanager.exception.handler.ErrorCode;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.checkpoints.CheckpointType;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.enums.PhoneNumberType;
import ru.ac.checkpointmanager.model.passes.PassTypeTime;

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

    public static final UUID CAR_ID = UUID.randomUUID();

    public static final String LICENSE_PLATE = "А420ВХ799";

    public static final UUID CROSSING_ID = UUID.randomUUID();

    public static final UUID PHONE_ID = UUID.randomUUID();

    public static final String PHONE_NUM = "+79167868124";

    public static final String JSON_ERROR_CODE = "$.errorCode";

    public static final String JSON_TIMESTAMP = "$.timestamp";

    public static final String JSON_VIOLATIONS_FIELD = "$.violations[%s].name";

    public static final String JSON_TITLE = "$.title";

    public static final String JSON_DETAIL = "$.detail";


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

    public static CarDTO getCarDto() {
        return new CarDTO(
                CAR_ID,
                LICENSE_PLATE,
                getCarBrand()
        );
    }

    public static PassUpdateDTO getPassUpdateDTO() {
        return new PassUpdateDTO(
                PASS_ID,
                "comment",
                PassTypeTime.ONETIME,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(7),
                null,
                getCarDto()
        );
    }

    public static PassCreateDTO getPassCreateDTO() {
        return new PassCreateDTO(
                PASS_ID,
                "comment",
                PassTypeTime.ONETIME,
                TERR_ID,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(7),
                null,
                getCarDto()
        );
    }

    public static PhoneDTO getPhoneDto() {
        return new PhoneDTO(
                PHONE_ID,
                PHONE_NUM,
                PhoneNumberType.HOME,
                USER_ID,
                "note"
        );
    }

    public static User getUser() {
        return Instancio.of(getInstancioUserModel()).create();
    }

    public static Model<User> getInstancioUserModel() {
        return Instancio.of(User.class)
                .ignore(Select.field("tokens"))
                .ignore(Select.field("numbers"))
                .ignore(Select.field("pass"))
                .ignore(Select.field("avatar"))
                .ignore(Select.field("territories"))
                .generate(Select.field("email"), gen -> gen.text().pattern("#a#a#a#a#a@example.com")).toModel();
    }

    public static String jsonStringFromObject(Object object) throws JsonProcessingException {
        ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
        return objectMapper.writeValueAsString(object);
    }

    public static void checkCommonValidationFields(ResultActions resultActions) throws Exception {
        resultActions.andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_ERROR_CODE)
                        .value(ErrorCode.VALIDATION.toString()));
    }

    private TestUtils() {
    }
}
