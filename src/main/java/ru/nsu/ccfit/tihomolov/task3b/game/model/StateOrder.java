package ru.nsu.ccfit.tihomolov.task3b.game.model;

import lombok.Setter;

public class StateOrder {
    @Setter
    private static int stateOrder = 1;

    public static int getStateOrder() {
        return stateOrder++;
    }
}
