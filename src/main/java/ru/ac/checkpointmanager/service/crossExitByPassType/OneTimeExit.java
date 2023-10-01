package ru.ac.checkpointmanager.service.crossExitByPassType;

public class OneTimeExit implements ExitPassAction {
    @Override
    public void exit() {
        System.out.println("exit one-time");
    }
}