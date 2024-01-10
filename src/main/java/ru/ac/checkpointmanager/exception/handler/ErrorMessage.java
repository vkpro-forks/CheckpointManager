package ru.ac.checkpointmanager.exception.handler;

public class ErrorMessage {


    public static final String PASS_EXCEPTION_TITLE = "Error related to passes";

    public static final String INTERNAL_SERVER_ERROR = "Internal server error, please check later";


    private ErrorMessage() {
        throw new AssertionError("No instance, please");
    }
}
