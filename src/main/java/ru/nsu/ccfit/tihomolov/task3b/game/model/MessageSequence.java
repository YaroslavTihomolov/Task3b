package ru.nsu.ccfit.tihomolov.task3b.game.model;

public class MessageSequence {
    private static long messageSequence = 1;

    public static long getMessageSequence() {
        return messageSequence++;
    }
}
