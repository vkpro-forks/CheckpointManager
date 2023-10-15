package ru.ac.checkpointmanager.configuration;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.Setter;

import java.sql.*;

@Setter
public class DBAppender extends AppenderBase<ILoggingEvent> {

    private Connection connection;
    private String url;
    private String username;
    private String password;



    @Override
    public void start() {
        if (url == null || username == null || password == null) {
            addError("One or more of the database connection parameters is null");
            return;
        }

        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            addError("Failed to establish database connection", e);
        }
        super.start();
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (!isStarted() || connection == null) {
            return;
        }

        Timestamp time = new Timestamp(eventObject.getTimeStamp());
        String logLevel = eventObject.getLevel().toString();
        String loggerName = eventObject.getLoggerName();
        String message = eventObject.getFormattedMessage();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO logs (time, level, logger, message) VALUES (?, ?, ?, ?)");

            preparedStatement.setTimestamp(1, time);
            preparedStatement.setString(2, logLevel);
            preparedStatement.setString(3, loggerName);
            preparedStatement.setString(4, message);

            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            addError("Failed to insert log record into the database", e);
        }
    }

    @Override
    public void stop() {
        try {
            connection.close();
        } catch (SQLException e) {
            addError("Failed to close database connection", e);
        }
        super.stop();
    }
}
