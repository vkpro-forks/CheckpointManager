package ru.ac.checkpointmanager.util;

public class UrlConstants {

    public static final String CAR_URL = "/api/v1/cars";

    public static final String CAR_USER_URL = "/api/v1/cars/users/{userId}";

    public static final String CAR_BRANDS_URL = CAR_URL + "/brands";

    public static final String CAR_BRANDS_URL_VAR = CAR_URL + "/brands/{brandId}";

    public static final String CAR_PHONE_URL = CAR_URL + "/phone";

    public static final String AVATAR_URL = "/api/v1/avatars";

    public static final String AVATAR_AVATARS_URL = "/api/v1/avatars/{avatarId}";

    public static final String AVATAR_USER_URL = "/api/v1/avatars/users/{userId}";

    public static final String AVATAR_TERRITORY_URL = "/api/v1/avatars/territories/{territoryId}";

    public static final String CROSSING_URL = "/api/v1/crossings";

    public static final String CHECKPOINT_URL = "/api/v1/checkpoints";

    public static final String PASS_URL = "/api/v1/passes";

    public static final String PASS_USER_URL = PASS_URL + "/users/{userId}";
    public static final String PASS_USER_TERRITORIES_URL = PASS_URL + "/users/{userId}/territories";

    public static final String PASS_URL_CANCEL = PASS_URL + "/{passId}/cancel";

    public static final String PASS_URL_ACTIVATE = PASS_URL + "/{passId}/activate";

    public static final String PASS_URL_UNWARNING = PASS_URL + "/{passId}/unwarning";

    public static final String PASS_URL_FAVORITE = PASS_URL + "/{passId}/favorite";

    public static final String PASS_URL_NOT_FAVORITE = PASS_URL + "/{passId}/not_favorite";

    public static final String PASS_URL_TERRITORY = PASS_URL + "/territories/{terrId}";

    public static final String TERR_URL = "/api/v1/territories";

    public static final String TERR_USERS_URL = TERR_URL + "/%s/users";

    public static final String TERR_ATTACH_DETACH_URL = TERR_URL + "/%s/users/%s";

    public static final String PHONE_URL = "/api/v1/phones";

    public static final String USER_URL = "/api/v1/users";

    public static final String USER_ROLE_URL = USER_URL + "/role";

    public static final String USER_TERR_URL = USER_URL + "/%s/territories";

    public static final String CONFIRM_URL = "/api/v1/confirm";

    public static final String CONFIRM_EMAIL_URL = CONFIRM_URL + "/email";

    public static final String CONFIRM_REG_URL = CONFIRM_URL + "/registration";

    public static final String AUTH_URL = "/api/v1/authentication";

    public static final String AUTH_LOGIN_URL = AUTH_URL + "/login";

    public static final String AUTH_REFRESH_TOKEN_URL = AUTH_URL + "/refresh-token";

    public static final String AUTH_REG_URL = AUTH_URL + "/registration";

    public static final String VISITOR_URL = "/api/v1/visitors";

    public static final String VISITOR_URL_ID = VISITOR_URL + "/{visitorId}";

    public static final String VISITOR_PHONE_URL = VISITOR_URL + "/phone";

    public static final String VISITOR_NAME_URL = VISITOR_URL + "/name";

    public static final String VISITOR_PASS_URL = VISITOR_URL + "/passes/{passId}";

    public static final String VISITOR_USER_URL = VISITOR_URL + "/users/{userId}";

    public static final String EVENT_URL = "/api/v1/events";

    public static final String EVENT_USER_URL = EVENT_URL + "/users/{userId}";

    public static final String EVENT_TERRITORY_URL = EVENT_URL + "/territories/{territoryId}";


    private UrlConstants() {
    }
}
