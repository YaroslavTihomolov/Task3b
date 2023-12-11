package ru.nsu.ccfit.tihomolov.task3b.exception;

public class FailedToSendMessageException extends RuntimeException {
    public FailedToSendMessageException(String message) {
        super(message);
    }
}
