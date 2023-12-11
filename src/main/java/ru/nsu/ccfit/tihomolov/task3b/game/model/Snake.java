package ru.nsu.ccfit.tihomolov.task3b.game.model;


import lombok.Getter;
import lombok.Setter;
import ru.nsu.ccfit.tihomolov.task3b.exception.WrongDirectionException;
import ru.nsu.ccfit.tihomolov.task3b.snakes.proto.SnakesProto;
import java.util.ArrayList;
import java.util.List;
import static ru.nsu.ccfit.tihomolov.task3b.context.Context.*;
import static ru.nsu.ccfit.tihomolov.task3b.snakes.proto.SnakesProto.GameState.Snake.SnakeState.ALIVE;


@Getter
public class Snake {
    public static final int EAT_FOOD = 1;
    public static final int NOT_EAT_FOOD = 0;
    public final Field field;
    private SnakesProto.GameState.Snake snake;
    @Setter
    private SnakesProto.Direction move;
    public static final int HEAD_INDEX = 0;

    public Snake(Field field, SnakesProto.GameState.Coord[] coords, int playerId) {
        this.field = field;
        this.snake = initSnake(coords, playerId);
    }

    public Snake(Field field, List<SnakesProto.GameState.Coord> coords, int playerId, SnakesProto.Direction headDirection) {
        this.field = field;
        this.snake = initSnake(coords, playerId, headDirection);
    }

    public List<SnakesProto.GameState.Coord> getPoints() {
        return snake.getPointsList();
    }

    public int getPlayerId() {
        return snake.getPlayerId();
    }

    public int getSize() {
        return snake.getPointsList().size();
    }

    private SnakesProto.GameState.Snake initSnake(SnakesProto.GameState.Coord[] coords, int playerId) {
        return SnakesProto.GameState.Snake.newBuilder()
                .setPlayerId(playerId)
                .addPoints(coords[0])
                .addPoints(coords[1])
                .setState(ALIVE)
                .setHeadDirection(chooseDirection(coords))
                .build();
    }

    private SnakesProto.GameState.Snake initSnake(List<SnakesProto.GameState.Coord> coords, int playerId, SnakesProto.Direction direction) {
        return SnakesProto.GameState.Snake.newBuilder()
                .setPlayerId(playerId)
                .addAllPoints(coords)
                .setState(ALIVE)
                .setHeadDirection(direction)
                .build();
    }

    private SnakesProto.Direction chooseDirection(SnakesProto.GameState.Coord[] coords) {
        if (coords[1].getX() - coords[0].getX() > 0) {
            return SnakesProto.Direction.LEFT;
        } else if (coords[1].getX() - coords[0].getX() < 0) {
            return SnakesProto.Direction.RIGHT;
        } else if (coords[1].getY() - coords[0].getY() > 0) {
            return SnakesProto.Direction.UP;
        } else {
            return SnakesProto.Direction.DOWN;
        }
    }

    private SnakesProto.GameState.Coord getNextCoord(SnakesProto.GameState.Coord coord, SnakesProto.Direction direction) {
        int x = coord.getX();
        int y = coord.getY();
        switch (direction.getNumber()) {
            case SnakesProto.Direction.UP_VALUE -> y -= 1;
            case SnakesProto.Direction.DOWN_VALUE -> y += 1;
            case SnakesProto.Direction.LEFT_VALUE -> x -= 1;
            case SnakesProto.Direction.RIGHT_VALUE -> x += 1;
            default -> throw new WrongDirectionException("");
        }
        y = (y + field.getHeight()) % field.getHeight();
        x = (x + field.getWidth()) % field.getWidth();
        return SnakesProto.GameState.Coord.newBuilder()
                .setX(x)
                .setY(y)
                .build();
    }

    public SnakesProto.Direction nextDirection(SnakesProto.Direction direction) {
        if (direction == null) return snake.getHeadDirection();
        int directionStatus = direction.getNumber() * snake.getHeadDirection().getNumber();
        if (directionStatus == 2 || directionStatus == 12) return snake.getHeadDirection();
        return direction;
    }

    public int move() {
        SnakesProto.Direction nextDirection = nextDirection(move);
        SnakesProto.GameState.Coord nextCellCoord = getNextCoord(snake.getPoints(HEAD_INDEX), nextDirection);
        List<Integer> nextCell = field.getCoordValue(nextCellCoord);
        ArrayList<SnakesProto.GameState.Coord> pointsList = new ArrayList<>(snake.getPointsList());
        int retVal = NOT_EAT_FOOD;

        if (nextCell.isEmpty() || !nextCell.contains(FOOD)) {
            field.deleteTailCoord(snake.getPoints(pointsList.size() - 1), snake.getPlayerId());
            pointsList.remove(pointsList.size() - 1);
        } else {
            retVal = EAT_FOOD;
            field.removeFood(nextCellCoord);
        }

        pointsList.add(HEAD_INDEX, nextCellCoord);
        field.addCoordSnake(nextCellCoord, snake.getPlayerId());
        move = null;

        snake = SnakesProto.GameState.Snake.newBuilder()
                .setPlayerId(snake.getPlayerId())
                .addAllPoints(pointsList)
                .setState(ALIVE)
                .setHeadDirection(nextDirection)
                .build();

        return retVal;
    }

    public void updateStatus(SnakesProto.GameState.Snake.SnakeState snakeState) {
        snake = SnakesProto.GameState.Snake.newBuilder()
                .setPlayerId(snake.getPlayerId())
                .addAllPoints(snake.getPointsList())
                .setState(snakeState)
                .setHeadDirection(snake.getHeadDirection())
                .build();
    }

    public boolean isHead(SnakesProto.GameState.Coord coord) {
        SnakesProto.GameState.Coord headCoord = snake.getPoints(HEAD_INDEX);
        return headCoord.getY() == coord.getY() && headCoord.getX() == coord.getX();
    }
}
