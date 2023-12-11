package ru.nsu.ccfit.tihomolov.task3b.context;

public class Context {
    private Context() {
        throw new IllegalStateException("Utility class");
    }
    public static final int BUFFER_SIZE = 65536;
    public static final int PERIOD = 4;

    public static final int CELL_FIRST_VALUE = 0;
    public static final int FOOD = 0;
    public static final float PERCENT_50 = 0.5f;
}
