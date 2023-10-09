package ru.ac.checkpointmanager.service.crossExitByPassType;

public class PermanentExit implements ExitPassAction {
    @Override
    public void exit() {
        System.out.println("exit permanent");
    }
}