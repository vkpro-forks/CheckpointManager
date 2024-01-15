package ru.ac.checkpointmanager.util;

public class UrlConstants {

    public static final String CAR_URL = "/api/v1/car";

    public static final String CAR_USER_URL = "/api/v1/car/user";

    public static final String CAR_BRANDS_URL = CAR_URL + "/brands";

    public static final String CAR_PHONE_URL = CAR_URL + "/phone";

    public static final String AVATAR_URL = "/api/v1/avatars";

    public static final String CROSSING_URL = "/api/v1/crossing";

    public static final String CHECKPOINT_URL = "/api/v1/checkpoint";

    public static final String PASS_URL = "/api/v1/pass";

    public static final String PASS_URL_CANCEL = PASS_URL + "/%s/cancel";

    public static final String PASS_URL_ACTIVATE = PASS_URL + "/%s/activate";

    public static final String PASS_URL_UNWARNING = PASS_URL + "/%s/unwarning";

    public static final String PASS_URL_FAVORITE = PASS_URL + "/%s/favorite";

    public static final String PASS_URL_NOT_FAVORITE = PASS_URL + "/%s/not_favorite";

    public static final String PASS_URL_TERRITORY = PASS_URL + "/territory/%s";

    public static final String TERR_URL = "/api/v1/territory";

    public static final String TERR_USERS_URL = TERR_URL + "/%s/users";

    public static final String TERR_ATTACH_DETACH_URL = TERR_URL + "/%s/user/%s";

    public static final String PHONE_URL = "/api/v1/phone";

    public static final String USER_URL = "/api/v1/user";

    public static final String USER_ROLE_URL = USER_URL + "/role";

    public static final String USER_TERR_URL = USER_URL + "/%s/territories";

    public static final String CONFIRM_URL = "/api/v1/confirm";

    public static final String CONFIRM_EMAIL_URL = CONFIRM_URL + "/email";

    public static final String CONFIRM_REG_URL = CONFIRM_URL + "/registration";

    public static final String AUTH_URL = "/api/v1/authentication";

    public static final String AUTH_LOGIN_URL = AUTH_URL + "/login";

    public static final String AUTH_REFRESH_TOKEN_URL = AUTH_URL + "/refresh-token";

    public static final String AUTH_REG_URL = AUTH_URL + "/registration";

    public static final String VISITOR_URL = "/api/v1/visitor";

    public static final String VISITOR_PHONE_URL = VISITOR_URL + "/phone";

    public static final String VISITOR_NAME_URL = VISITOR_URL + "/name";


    private UrlConstants() {

    }
}
