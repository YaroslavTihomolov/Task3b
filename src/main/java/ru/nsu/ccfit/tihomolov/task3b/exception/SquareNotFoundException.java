package ru.nsu.ccfit.tihomolov.task3b.exception;

import java.io.IOException;

public class SquareNotFoundException extends IOException {
    public SquareNotFoundException(String message) {
        super(message);
    }
}
