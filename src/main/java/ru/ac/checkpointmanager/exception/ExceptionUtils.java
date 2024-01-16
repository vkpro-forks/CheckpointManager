package ru.ac.checkpointmanager.exception;

public class ExceptionUtils {

    public static final String USER_NOT_FOUND_MSG = "User with [id: %s] not found";

    public static final String TERRITORY_NOT_FOUND_MSG = "Territory with [id: %s] not found";

    public static final String USER_TER_REL_MSG = "Reject: user [%s] not have permission to create passes for territory [%s]";

    public static final String INVALID_EMAIL_TOKEN_MSG = "Invalid or expired token %s";

    public static final String EMAIL_EXISTS = "[Email: %s] already exists";

    public static final String PHONE_EXISTS = "Phone number %s already exist";

    public static final String CACHING_FAILED = "Caching failed: {}";

    public static final String PASS_ALREADY_USED = "OnetimePass [%s] has already been used, it is not possible to enter";

    public static final String UNSUPPORTED_PASS_TYPE = "Unsupported pass time type - %s";

    public static final String INACTIVE_PASS = "The pass is not active now %s";

    public static final String PASS_NOT_FOUND = "Pass [%s] not found";

    public static final String PASS_MISMATCHED_TERRITORY = "Pass [%s] is issued to another territory: [%s]," +
            "checkpoint here: [%s]";

    public static final String CHECKPOINT_NOT_FOUND = "[Checkpoint with id: %s] not found";

    public static final String AVATAR_PROCESSING_ERROR = "Error processing avatar: %s";
    public static final String VISITOR_NOT_FOUND = "Visitor with [id %s] not found";

    public static final String AVATAR_NOT_FOUND_FOR_USER = "Avatar for [user id: %s] not found";

    public static final String AVATAR_NOT_FOUND = "Avatar with [id: %s] not found";

    private ExceptionUtils() {
        throw new AssertionError("No instances, please");
    }

}
