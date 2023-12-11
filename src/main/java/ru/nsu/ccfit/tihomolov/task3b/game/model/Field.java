package ru.nsu.ccfit.tihomolov.task3b.game.model;

import lombok.Getter;
import ru.nsu.ccfit.tihomolov.task3b.exception.SquareNotFoundException;
import ru.nsu.ccfit.tihomolov.task3b.exception.WrongDirectionException;
import ru.nsu.ccfit.tihomolov.task3b.snakes.proto.SnakesProto;

import java.util.*;

import static ru.nsu.ccfit.tihomolov.task3b.context.Context.FOOD;


public class Field {
    private final ArrayList<LinkedList<Integer>> curField;
    private final List<SnakesProto.GameState.Coord> food;
    @Getter
    private final int width;
    @Getter
    private final int height;
    @Getter
    private final int size;
    private static final int SQUARE_NOT_SUITABLE = 2;
    private static final int EMPTY_SQUARE_SIZE = 5;
    private static final int SHIFT_TO_CENTER = 2;
    private static final int NUMBER_DIRECTION_OPTION = 4;
    private final Random random = new Random();

    public Field(int width, int height, List<SnakesProto.GameState.Coord> food) {
        this.food = food;
        this.width = width;
        this.height = height;
        this.size = width * height;
        this.curField = new ArrayList<>(width * height);
        for (int i = 0; i < size; i++) {
            curField.add(new LinkedList<>());
        }
    }

    public void updateFood() {
        food.forEach(
                this::addFoodToCord
        );
    }

    public void removeFood(SnakesProto.GameState.Coord coord) {
        curField.get(coordToIndex(coord)).remove(Integer.valueOf(FOOD));
        food.remove(coord);
    }

    public void addCoordSnake(SnakesProto.GameState.Coord coord, int snakeID) {
        curField.get(coordToIndex(coord)).add(snakeID);
    }

    public void deleteTailCoord(SnakesProto.GameState.Coord coord, Integer snakeId) {
        curField.get(coordToIndex(coord)).remove(snakeId);
    }

    private int checkSquare(int coord) {
        int center = coord + SHIFT_TO_CENTER * width + SHIFT_TO_CENTER;
        if (!curField.get(center).isEmpty()) {
            return SQUARE_NOT_SUITABLE;
        }

        for (int i = 1; i < EMPTY_SQUARE_SIZE; i++) {
            for (int j = 0; j < EMPTY_SQUARE_SIZE; j++) {
                if (!curField.get(coord + i * width + j).isEmpty() && curField.get(coord + i * width + j).get(0) != FOOD) {
                    return j;
                }
            }
        }

        if (!curField.get(center - 1).isEmpty() && !curField.get(center + 1).isEmpty() &&
                !curField.get(center - width).isEmpty() && !curField.get(center + width).isEmpty()) {
            return SQUARE_NOT_SUITABLE;
        }

        return center;
    }


    private SnakesProto.GameState.Coord findPlaceForSpace() throws SquareNotFoundException {
        int coord;
        int halfWidth = width / 2;
        int halfHeight = height / 2;
        for (int i = 0; i < height; i++) {
            if (height - (i + halfHeight) < 4) continue;
            int j = 0;
            while (j < width) {
                if (width - (i + halfWidth) < 4) continue;
                int offsetX = (j + halfWidth) % width;
                int offsetY = (i + halfHeight) % height;
                if ((coord = checkSquare(offsetY * width + offsetX)) > width) {
                    return SnakesProto.GameState.Coord.newBuilder()
                            .setX(offsetX)
                            .setY(offsetY)
                            .build();
                }
                j += coord + 1;
            }
        }
        throw new SquareNotFoundException("Square not Found");
    }

    public SnakesProto.GameState.Coord indexToCoord(int index) {
        return SnakesProto.GameState.Coord.newBuilder()
                .setY(index / width)
                .setX(index % width)
                .build();
    }

    private int directionCoord(int value, int direction) {
        switch (direction) {
            case SnakesProto.Direction.UP_VALUE -> {
                return value - width;
            }
            case SnakesProto.Direction.DOWN_VALUE -> {
                return value + width;
            }
            case SnakesProto.Direction.LEFT_VALUE -> {
                return value - 1;
            }
            case SnakesProto.Direction.RIGHT_VALUE -> {
                return value + 1;
            }
            default -> throw new WrongDirectionException("");
        }
    }

    public List<Integer> getCoordValue(SnakesProto.GameState.Coord coord) {
        int index = coord.getY() * width + coord.getX();
        return curField.get(index);
    }

    private SnakesProto.GameState.Coord findCoordForTail(SnakesProto.GameState.Coord coord) {
        int tailCoord;
        int index = coord.getY() * width + coord.getX();
        int startValue = random.nextInt(NUMBER_DIRECTION_OPTION);
        while (!curField.get(tailCoord = directionCoord(index, startValue + 1)).isEmpty()) {
            startValue = (startValue + 1) % NUMBER_DIRECTION_OPTION;
        }
        int yPos = tailCoord / width;

        return SnakesProto.GameState.Coord.newBuilder()
                .setY(yPos)
                .setX(tailCoord % width)
                .build();
    }

    private int coordToIndex(SnakesProto.GameState.Coord coord) {
        return coord.getY() * width + coord.getX();
    }

    public SnakesProto.GameState.Coord[] placeForSneak(int sneakId) throws SquareNotFoundException {
        SnakesProto.GameState.Coord[] coords = new SnakesProto.GameState.Coord[2];
        coords[0] = findPlaceForSpace();
        coords[1] = findCoordForTail(coords[0]);
        curField.get(coordToIndex(coords[0])).add(sneakId);
        curField.get(coordToIndex(coords[1])).add(sneakId);
        return coords;
    }

    public void addFoodToCord(SnakesProto.GameState.Coord coord) {
        curField.get(coordToIndex(coord)).add(FOOD);
    }

    public void addFoodToCord(int index) {
        if (index < 0 || index > size) return;
        addFoodToCord(indexToCoord(index));
    }

    public List<Integer> getCoordValue(int index) {
        return getCoordValue(indexToCoord(index));
    }
}
