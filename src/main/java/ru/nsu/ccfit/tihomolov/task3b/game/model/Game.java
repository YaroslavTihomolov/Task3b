package ru.nsu.ccfit.tihomolov.task3b.game.model;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.ccfit.tihomolov.task3b.game.controller.Observer;
import ru.nsu.ccfit.tihomolov.task3b.exception.SquareNotFoundException;
import ru.nsu.ccfit.tihomolov.task3b.network.storage.HostNetworkInfo;
import ru.nsu.ccfit.tihomolov.task3b.proto.SnakesProto;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static ru.nsu.ccfit.tihomolov.task3b.context.Context.PERCENT_50;


@Slf4j
public class Game implements Observable, Runnable {
    @Getter
    private final String name;
    private SnakesProto.GameState gameState;
    private Observer gameController;
    private final Field field;
    private final Map<HostNetworkInfo, Integer> playersId = new HashMap<>();
    private final Map<Integer, SnakesProto.GamePlayer> players = new HashMap<>();
    private final Map<Integer, Snake> snakes = new HashMap<>();
    private int playerId = 1;
    private LinkedList<SnakesProto.GameState.Coord> food = new LinkedList<>();
    private final SnakesProto.GameConfig gameConfig;
    private final int foodStatic;

    public Game(SnakesProto.GameConfig config, String playerName, String gameName) {
        this.name = gameName;

        this.gameConfig = config;
        this.field = new Field(config.getWidth(), config.getHeight(), food);

        try {
            this.playersId.put(new HostNetworkInfo(InetAddress.getLocalHost(), 0), playerId);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        this.players.put(playerId, Player.initMaster(playerId, playerName));

        try {
            this.snakes.put(playerId, new Snake(field, field.placeForSneak(playerId), playerId));
        } catch (SquareNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.foodStatic = config.getFoodStatic();

        addFood(foodStatic + playerId);
        ++this.playerId;
    }

    private HostNetworkInfo initHostNetworkInfo(String ip, int port) {
        try {
            return new HostNetworkInfo(InetAddress.getByName(ip), port);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public Game(SnakesProto.GameState gameState, SnakesProto.GameConfig config, HostNetworkInfo lastMasterNetworkInfo,
                HostNetworkInfo selfHostNetworkInfo, String gameName) {
        this.name = gameName;

        this.gameConfig = config;
        food = new LinkedList<>(gameState.getFoodsList());
        this.field = new Field(config.getWidth(), config.getHeight(), food);
        field.updateFood();


        gameState.getPlayers().getPlayersList()
                .forEach(player -> {
                    HostNetworkInfo hostNetworkInfo;
                    SnakesProto.NodeRole newRole = player.getRole();
                    if (!player.hasIpAddress()) {
                        hostNetworkInfo = lastMasterNetworkInfo;
                        newRole = SnakesProto.NodeRole.NORMAL;
                    } else if (HostNetworkInfo.handleIp(player.getIpAddress()).equals(HostNetworkInfo.handleIp(selfHostNetworkInfo.getIp().toString())) &&
                            player.getPort() == selfHostNetworkInfo.getPort()) {
                        try {
                            newRole = SnakesProto.NodeRole.MASTER;
                            hostNetworkInfo = new HostNetworkInfo(InetAddress.getLocalHost(), 0);
                        } catch (UnknownHostException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        String ip = HostNetworkInfo.handleIp(player.getIpAddress());
                        hostNetworkInfo = initHostNetworkInfo(ip, player.getPort());
                    }
                    playersId.put(hostNetworkInfo, player.getId());
                    players.put(player.getId(), Player.updateRole(player, newRole));
                    ++this.playerId;
                });

        gameState.getSnakesList()
                .forEach(snake -> snakes.put(snake.getPlayerId(), new Snake(field, snake.getPointsList(), snake.getPlayerId(), snake.getHeadDirection())));

        this.foodStatic = config.getFoodStatic();
    }

    private int getEmptyCellsCount() {
        int notEmptyCell = food.size();
        notEmptyCell += snakes.values().stream().mapToInt(Snake::getSize).sum();
        return field.getSize() - notEmptyCell;
    }

    private void addFood(int count) {
        if (count < 0) return;
        Random random = new Random();
        int value;
        for (int i = 0; i < count - 1 && getEmptyCellsCount() > 0; i++) {
            while (!field.getCoordValue((value = random.nextInt(field.getSize()))).isEmpty()) {
            }
            field.addFoodToCord(value);
            food.add(field.indexToCoord(value));
        }
    }

    public SnakesProto.GameMessage.AnnouncementMsg getAnnouncementMsg() {
        return SnakesProto.GameMessage.AnnouncementMsg.newBuilder()
                .addGames(SnakesProto.GameAnnouncement.newBuilder()
                        .setPlayers(SnakesProto.GamePlayers.newBuilder().addAllPlayers(players.values().stream().toList()))
                        .setConfig(gameConfig)
                        .setCanJoin(true)
                        .setGameName(name)
                        .build())
                .build();
    }

    private SnakesProto.GameState getGameState() {
        return SnakesProto.GameState.newBuilder()
                .setStateOrder(StateOrder.getStateOrder())
                .addAllFoods(food)
                .addAllSnakes(snakes.values().stream().map(Snake::getSnake).toList())
                .setPlayers(SnakesProto.GamePlayers.newBuilder().addAllPlayers(players.values()))
                .build();
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                makeMove();
                checkCrashes();
                addFood();
                notifyGameState();
                Thread.sleep(gameConfig.getStateDelayMs());
            } catch (InterruptedException e) {
                log.error(e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

    public void addFood() {
        addFood(foodStatic + playerId - food.size());
    }


    public int addPlayer(SnakesProto.GameMessage.JoinMsg joinMsg, HostNetworkInfo hostNetworkInfo, SnakesProto.NodeRole role) {
        int id;
        try {
            id = playerId;
            playersId.put(hostNetworkInfo, playerId);
            players.put(playerId, Player.init(playerId, joinMsg.getPlayerType(), joinMsg.getPlayerName(), role,
                    String.valueOf(hostNetworkInfo.getIp()).substring(1), hostNetworkInfo.getPort()));
            if (joinMsg.getRequestedRole() != SnakesProto.NodeRole.VIEWER) {
                snakes.put(playerId, new Snake(field, field.placeForSneak(playerId), playerId));
            }
            ++this.playerId;
        } catch (SquareNotFoundException e) {
            return 0;
        }
        return id;
    }

    public void addMove(HostNetworkInfo hostNetworkInfo, SnakesProto.Direction move) {
        Integer playerId;
        playerId = playersId.get(hostNetworkInfo);
        snakes.get(playerId).setMove(move);
    }

    public void makeMove() {
        snakes.values().forEach(
                snake -> {
                    int retValue = snake.move();
                    if (retValue == Snake.EAT_FOOD) {
                        SnakesProto.GamePlayer snakePlayer = players.get(snake.getPlayerId());
                        if (snakePlayer.getRole() == SnakesProto.NodeRole.MASTER) {
                            players.put(snakePlayer.getId(), Player.updateMasterScore(snakePlayer, 1));
                        } else {
                            players.put(snakePlayer.getId(), Player.updateScore(snakePlayer, 1));
                        }
                    }
                });
    }

    private void checkCellForSnakes(Snake snakeToCheck, LinkedList<Integer> snakesToRemove) {
        SnakesProto.GameState.Snake snake = snakeToCheck.getSnake();
        SnakesProto.GameState.Coord snakeHeadCoord = snake.getPoints(Snake.HEAD_INDEX);
        List<Integer> cellContentSnake = field.getCoordValue(snakeHeadCoord);
        int snakeOnCell = cellContentSnake.size();

        for (Integer cellContent : cellContentSnake) {
            if (cellContent != snake.getPlayerId()) {
                Snake crashSnake = snakes.get(cellContent);
                if (crashSnake != null && crashSnake.isHead(snakeHeadCoord)) {
                    deleteFromField(snakes.get(cellContent));
                    snakesToRemove.add(cellContent);
                } else if (crashSnake != null) {
                    SnakesProto.GamePlayer crashSnakePlayer = players.get(crashSnake.getPlayerId());
                    players.put(crashSnakePlayer.getId(), Player.updateScore(crashSnakePlayer, 1));
                }
            }
        }

        if (snakeOnCell != 1) {
            deleteFromField(snakeToCheck);
            snakesToRemove.add(snakeToCheck.getPlayerId());
        }

    }

    public void checkCrashes() {
        LinkedList<Integer> snakesToRemove = new LinkedList<>();
        snakes.values().forEach(snake -> checkCellForSnakes(snake, snakesToRemove));
        snakesToRemove.forEach(snakes::remove);
        snakesToRemove.forEach(players::remove);
    }

    public void deleteFromField(Snake snake) {
        Random random = new Random();
        field.deleteTailCoord(snake.getPoints().get(0), snake.getPlayerId());
        snake.getPoints()
                .stream()
                .skip(1)
                .forEach(coord -> {
                    field.deleteTailCoord(coord, snake.getPlayerId());
                    if (random.nextFloat() > PERCENT_50) {
                        field.addFoodToCord(coord);
                        food.add(coord);
                    }
                });
    }

    public void updatePlayerRole(HostNetworkInfo hostNetworkInfo, SnakesProto.NodeRole role) {
        Integer playerToChangeId = playersId.get(hostNetworkInfo);
        players.put(playerToChangeId, Player.updateRole(players.get(playerToChangeId), role));
    }

    public void handleAfkPlayer(HostNetworkInfo hostNetworkInfo) {
        Integer afkPlayerId = playersId.get(hostNetworkInfo);
        SnakesProto.GamePlayer afkPlayer = players.get(afkPlayerId);
        if (afkPlayer != null && afkPlayer.getRole() != SnakesProto.NodeRole.VIEWER) {
            players.put(afkPlayerId, Player.updateRole(afkPlayer, SnakesProto.NodeRole.VIEWER));
            Snake afkPlayerSnake = snakes.get(afkPlayerId);
            afkPlayerSnake.updateStatus(SnakesProto.GameState.Snake.SnakeState.ZOMBIE);
        } else {
            players.remove(afkPlayerId);
            playersId.remove(hostNetworkInfo);
        }
    }

    @Override
    public void addObserver(Observer observer) {
        gameController = observer;
    }

    @Override
    public void notifyGameState() {
        gameController.updateGameState(getGameState());
    }

    @Override
    public void updateUpdateAnnouncementMsg() {
        gameController.updateAnnouncementMsg(getAnnouncementMsg());
    }
}
