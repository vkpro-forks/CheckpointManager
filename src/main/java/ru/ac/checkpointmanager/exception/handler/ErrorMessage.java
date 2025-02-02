package ru.ac.checkpointmanager.exception.handler;

public class ErrorMessage {


    public static final String PASS_EXCEPTION_TITLE = "Error related to passes";

    public static final String INTERNAL_SERVER_ERROR = "Internal server error, please check later";

    public static final String OBJECT_NOT_FOUND = "Object not found";

    public static final String OBJECT_ALREADY_EXISTS = "Object already exists";

    public static final String WRONG_ARGUMENT_PASSED = "Wrong type of argument passed";

    public static final String MISSING_REQUEST_PARAM = "No required request param passed";

    public static final String DONATION_ERROR = "Something went wrong with donation payment";

    private ErrorMessage() {
        throw new AssertionError("No instance, please");
    }
}
