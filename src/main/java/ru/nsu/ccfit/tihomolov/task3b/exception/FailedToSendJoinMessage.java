package ru.nsu.ccfit.tihomolov.task3b.exception;

public class FailedToSendJoinMessage extends RuntimeException {
    public FailedToSendJoinMessage(String message) {
        super(message);
    }
}
