package ru.ac.checkpointmanager.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.dto.CarBrandDTO;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.dto.CheckpointDTO;
import ru.ac.checkpointmanager.dto.CrossingDTO;
import ru.ac.checkpointmanager.dto.PhoneDTO;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.dto.passes.PassUpdateDTO;
import ru.ac.checkpointmanager.dto.user.AuthenticationRequest;
import ru.ac.checkpointmanager.dto.user.ChangeEmailRequest;
import ru.ac.checkpointmanager.dto.user.ChangePasswordRequest;
import ru.ac.checkpointmanager.dto.user.ConfirmChangeEmail;
import ru.ac.checkpointmanager.dto.user.ConfirmRegistration;
import ru.ac.checkpointmanager.dto.user.RefreshTokenDTO;
import ru.ac.checkpointmanager.dto.user.UserAuthDTO;
import ru.ac.checkpointmanager.dto.user.UserPutDTO;
import ru.ac.checkpointmanager.dto.user.UserResponseDTO;
import ru.ac.checkpointmanager.exception.handler.ErrorCode;
import ru.ac.checkpointmanager.model.Phone;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.avatar.Avatar;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;
import ru.ac.checkpointmanager.model.checkpoints.CheckpointType;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.enums.PhoneNumberType;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.model.passes.PassTimeType;
import ru.ac.checkpointmanager.security.CustomAuthenticationToken;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestUtils {

    public static final Long CAR_BRAND_ID = 1000L;

    public static final String CAR_BRAND_ID_STR = CAR_BRAND_ID.toString();

    public static final UUID USER_ID = UUID.randomUUID();

    public static final String USER_NAME = "name";

    public static final String ERROR_MESSAGE_SHOULD = "This check should be not here";

    public static final UUID PASS_ID = UUID.randomUUID();

    public static final UUID CHECKPOINT_ID = UUID.randomUUID();

    public static final UUID CROSSING_ID = UUID.randomUUID();

    public static final String CHECKPOINT_NAME = "ch_name";

    public static final UUID TERR_ID = UUID.randomUUID();

    public static final UUID AVATAR_ID = UUID.randomUUID();

    public static final String DEFAULT_MEDIA_TYPE = "image/jpeg";

    public static final String DEFAULT_FILE_PATH = "/path/to/image.jpg";

    public static final String TERR_NAME = "Territory";

    public static final UUID CAR_ID = UUID.randomUUID();

    public static final String LICENSE_PLATE = "А420ВХ799";

    public static final UUID PHONE_ID = UUID.randomUUID();

    public static final String PHONE_NUM = "+79167868124";

    public static final String JSON_ERROR_CODE = "$.errorCode";

    public static final String JSON_TIMESTAMP = "$.timestamp";

    public static final String JSON_VIOLATIONS_FIELD = "$.violations[%s].name";

    public static final String JSON_TITLE = "$.title";

    public static final String JSON_DETAIL = "$.detail";

    public static final UUID EMAIL_TOKEN = UUID.randomUUID();

    public static final String EMAIL_STRING_TOKEN = EMAIL_TOKEN.toString();

    public static final UUID VISITOR_ID = UUID.randomUUID();
    public static final String PASSWORD = "password";

    public static final String EMAIL = "123@123.com";
    public static final String NEW_EMAIL = "new@com.com";
    public static final String FULL_NAME = "Username";
    public static final String NEW_PASSWORD = "new_password";

    public static final String AUTH_HEADER = "Authorization";

    public static final String BEARER = "Bearer ";


    public static CarBrand getCarBrand() {
        CarBrand carBrand = new CarBrand();
        carBrand.setId(CAR_BRAND_ID);
        carBrand.setBrand("Buhanka");
        return carBrand;
    }

    public static CarBrandDTO getCarBrandDTO() {
        return new CarBrandDTO("Buhanka");
    }

    public static CrossingDTO getCrossingDTO() {
        return new CrossingDTO(
                CROSSING_ID,
                PASS_ID,
                CHECKPOINT_ID,
                ZonedDateTime.now(),
                Direction.IN
        );
    }

    public static Territory getTerritory() {
        return Instancio.of(Territory.class)
                .ignore(Select.field("users"))
                .ignore(Select.field(Territory::getPass))
                .ignore(Select.field(Territory::getCheckpoints))
                .set(Select.field(Territory::getName), TERR_NAME)
                .set(Select.field(Territory::getId), TERR_ID)
                .ignore(Select.field(Territory::getAvatar))
                .create();
    }

    public static TerritoryDTO getTerritoryDTO() {
        return new TerritoryDTO(
                TERR_ID,
                TERR_NAME,
                "note"
        );
    }

    public static Territory getTerritoryForDB() {
        Territory territory = new Territory();
        territory.setName(TERR_NAME);
        return territory;
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
                getCarBrandDTO(),
                PHONE_NUM
        );
    }

    public static PassUpdateDTO getPassUpdateDTO() {
        return new PassUpdateDTO(
                PASS_ID,
                "comment",
                PassTimeType.ONETIME,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(7),
                null,
                getCarDto()
        );
    }

    public static PassCreateDTO getPassCreateDTOWithCar() {
        return new PassCreateDTO(
                USER_ID,
                "comment",
                PassTimeType.ONETIME,
                TERR_ID,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(7),
                null,
                getCarDto()
        );
    }

    public static PassCreateDTO getPassCreateDTOWithVisitor() {
        return new PassCreateDTO(
                USER_ID,
                "comment",
                PassTimeType.ONETIME,
                TERR_ID,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(7),
                getVisitorDTO(),
                null
        );
    }

    public static PhoneDTO getPhoneDto() {
        return new PhoneDTO(
                PHONE_ID,
                PHONE_NUM,
                PhoneNumberType.MOBILE,
                USER_ID,
                null
        );
    }

    public static Phone getPhoneForDB() {
        Phone phone = new Phone();
        phone.setNumber(PHONE_NUM);
        phone.setType(PhoneNumberType.MOBILE);
        return phone;
    }

    public static User getUserForDB() {
        User user = new User();
        user.setFullName(FULL_NAME);
        user.setEmail(EMAIL);
        user.setPassword(PASSWORD);
        return user;
    }

    public static User getUser() {
        return Instancio.of(getInstancioUserModel()).create();
    }

    public static Model<User> getInstancioUserModel() {
        return Instancio.of(User.class)
                .ignore(Select.field("numbers"))
                .ignore(Select.field("pass"))
                .ignore(Select.field("avatar"))
                .ignore(Select.field("territories"))
                .generate(Select.field("email"), gen -> gen.text().pattern("#a#a#a#a#a@example.com")).toModel();
    }

    public static UserPutDTO getUserPutDTO() {
        return new UserPutDTO(
                USER_ID,
                "Vasin Vasya Petya",
                "+79167868345"
        );
    }

    public static UserResponseDTO getUserResponseDTO() {
        return new UserResponseDTO(
                USER_ID,
                "Sashulka",
                PHONE_NUM,
                EMAIL,
                false,
                Role.ADMIN,
                null
        );
    }

    public static ChangePasswordRequest getChangePasswordRequest() {
        return new ChangePasswordRequest(
                PASSWORD,
                NEW_PASSWORD,
                NEW_PASSWORD
        );
    }

    public static ConfirmChangeEmail getConfirmChangeEmail() {
        return new ConfirmChangeEmail(
                EMAIL,
                NEW_EMAIL,
                EMAIL_STRING_TOKEN
        );
    }

    public static ChangeEmailRequest getChangeEmailRequest() {
        return new ChangeEmailRequest(NEW_EMAIL);
    }

    public static ConfirmRegistration getConfirmRegistration() {
        return new ConfirmRegistration(
                FULL_NAME,
                EMAIL,
                PASSWORD,
                EMAIL_STRING_TOKEN
        );
    }

    public static AuthenticationRequest getAuthenticationRequest() {
        return new AuthenticationRequest(EMAIL, PASSWORD);
    }

    public static RefreshTokenDTO getRefreshTokenDTO() {
        return new RefreshTokenDTO(getJwt(86400000, EMAIL, List.of("ROLE_ADMIN"), true, true));
    }

    public static String getSimpleValidAccessToken() {
        return getJwt(60000, EMAIL, List.of("ADMIN"), false, true);
    }

    public static String getJwt(Integer expired, String username, List<String> roles, boolean isRefresh,
                                boolean withIdClaim) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", roles);

        if (isRefresh) {
            claims.put("refresh", true);
        }
        if (withIdClaim) {
            claims.put("id", USER_ID);
        }
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expired))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public static VisitorDTO getVisitorDTO() {
        return new VisitorDTO(
                VISITOR_ID,
                EMAIL,
                PHONE_NUM,
                "note"
        );
    }

    public static PassAuto getSimpleActiveOneTimePassAutoFor3Hours(User user, Territory territory, Car car) {
        PassAuto passAuto = new PassAuto();
        passAuto.setStartTime(LocalDateTime.now());
        passAuto.setEndTime(LocalDateTime.now().plusHours(3));
        passAuto.setId(UUID.randomUUID());
        passAuto.setTimeType(PassTimeType.ONETIME);
        passAuto.setDtype("AUTO");
        passAuto.setStatus(PassStatus.ACTIVE);
        passAuto.setCar(car);
        passAuto.setUser(user);
        passAuto.setTerritory(territory);
        return passAuto;
    }

    public static Checkpoint getCheckpoint(CheckpointType type, Territory territory) {
        Checkpoint checkpoint = new Checkpoint();
        checkpoint.setName(CHECKPOINT_NAME);
        checkpoint.setType(type);
        checkpoint.setTerritory(territory);
        return checkpoint;
    }

    public static Car getCar(CarBrand carBrand) {
        Car car = new Car();
        car.setLicensePlate(TestUtils.LICENSE_PLATE);
        car.setBrand(carBrand);
        car.setId(TestUtils.CAR_ID);
        return car;
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

    public static void checkNotFoundFields(ResultActions resultActions) throws Exception {
        resultActions.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_ERROR_CODE)
                        .value(ErrorCode.NOT_FOUND.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_TIMESTAMP).isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_TITLE).isNotEmpty());
    }

    private static Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(getKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static String getKey() {
        return "8790D58F7205C4C250CD67DD6D9B6F8B20D2E928FFAA6D4A2BEB2AD2189B01D1";
    }

    public static CustomAuthenticationToken getAuthToken(User user) {
        Collection<? extends GrantedAuthority> authorities = List
                .of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        return new CustomAuthenticationToken(user, null, user.getId(), authorities);
    }

    private TestUtils() {
    }

    public static UserAuthDTO getUserAuthDTO() {
        return new UserAuthDTO(
                TestUtils.USER_ID,
                TestUtils.FULL_NAME,
                TestUtils.EMAIL,
                TestUtils.PASSWORD
        );
    }

    public static Avatar createTestAvatar() {
        Avatar avatar = new Avatar();
        avatar.setMediaType(DEFAULT_MEDIA_TYPE);
        avatar.setFilePath(DEFAULT_FILE_PATH);
        avatar.setFileSize(1024L);
        avatar.setPreview(new byte[10]);
        return avatar;
    }

    public static Avatar createTestAvatarWithEmptyImageData() {
        Avatar avatar = new Avatar();
        avatar.setId(UUID.randomUUID());
        avatar.setPreview(new byte[0]);
        avatar.setMediaType(null);
        avatar.setFileSize(0L);
        return avatar;
    }

    public static Avatar createTestAvatarNotWithPreview() {
        Avatar avatar = new Avatar();
        avatar.setId(AVATAR_ID);
        avatar.setMediaType(DEFAULT_MEDIA_TYPE);
        avatar.setFilePath(DEFAULT_FILE_PATH);
        avatar.setFileSize(1024L);
        avatar.setPreview(null);
        return avatar;
    }

}
