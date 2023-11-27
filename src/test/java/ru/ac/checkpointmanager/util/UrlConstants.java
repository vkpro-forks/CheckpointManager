package ru.ac.checkpointmanager.util;

public class UrlConstants {

    public static final String CAR_URL = "/api/v1/car";

    public static final String CAR_BRANDS_URL = CAR_URL + "/brands";

    public static final String AVATAR_URL = "/api/v1/avatars";

    public static final String AVATAR_URL_PREVIEW = AVATAR_URL + "/preview";

    public static final String CROSSING_URL = "/api/v1/crossing";

    public static final String CROSSING_MARK_URL = CROSSING_URL + "/mark";

    public static final String CHECKPOINT_URL = "/api/v1/checkpoint";

    public static final String PASS_URL = "/api/v1/pass";

    public static final String PASS_URL_CANCEL = PASS_URL + "/%s/cancel";

    public static final String PASS_URL_ACTIVATE = PASS_URL + "/%s/activate";

    public static final String PASS_URL_UNWARNING = PASS_URL + "/%s/unwarning";

    public static final String PASS_URL_FAVORITE = PASS_URL + "/%s/favorite";

    public static final String PASS_URL_NOT_FAVORITE = PASS_URL + "/%s/not_favorite";

    public static final String PASS_URL_TERRITORY = PASS_URL + "/territory/%s";

    public static final String TERR_URL = "/api/v1/territory";

    public static final String TERR_USERS_URL = TERR_URL+"/%s/users";


    private UrlConstants() {

    }
}
