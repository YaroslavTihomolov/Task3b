package ru.nsu.ccfit.tihomolov.task3b.game.controller;

import javafx.application.Platform;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.ccfit.tihomolov.task3b.game.model.GameMessageCreator;
import ru.nsu.ccfit.tihomolov.task3b.game.model.StateOrder;
import ru.nsu.ccfit.tihomolov.task3b.game.ui.View;
import ru.nsu.ccfit.tihomolov.task3b.game.model.Game;
import ru.nsu.ccfit.tihomolov.task3b.network.NetworkController;
import ru.nsu.ccfit.tihomolov.task3b.network.storage.HostNetworkInfo;
import ru.nsu.ccfit.tihomolov.task3b.network.storage.NodeInfo;
import ru.nsu.ccfit.tihomolov.task3b.snakes.proto.SnakesProto;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class GameController implements Observer, GameListObservable {
    private final LinkedList<GameListObserver> gameListObservers = new LinkedList<>();
    @Setter
    private NetworkController networkController;
    private Game game;
    private String playerName;
    private SnakesProto.GameState lastGameState;
    private JoinController joinController;
    private final View view;
    private SnakesProto.NodeRole role;
    private SnakesProto.GameAnnouncement curGameInfo;
    private int curGameStateOrder = Integer.MIN_VALUE;
    private int selfPlayerId;

    public GameController(View view) {
        this.view = view;
    }

    public void openJoinWindow(SnakesProto.GameMessage.AnnouncementMsg announcementMsg) {
        this.joinController = view.createJoinWindow();
        this.joinController.setGameController(this);
        this.joinController.setAnnouncementMsg(announcementMsg);
    }

    public void printErrorMessage() {
        this.joinController.printErrorText();
    }

    private void gameMasterRoutine(SnakesProto.GameConfig gameConfig) {
        this.game.addObserver(this);
        Thread thread = new Thread(game);
        thread.start();
        this.role = SnakesProto.NodeRole.MASTER;
        networkController.setSelfRole(role);
        startAnnouncementMsg(game.getAnnouncementMsg());
        try {
            networkController.setMasterInfo(new HostNetworkInfo(InetAddress.getLocalHost(), 0));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public void creatGame(SnakesProto.GameConfig gameConfig, String playerName, String gameName) {
        this.game = new Game(gameConfig, playerName, gameName);
        networkController.startMessageScheduler(gameConfig.getStateDelayMs());
        this.view.openGameWindow(gameConfig, this);
        networkController.startPing(gameConfig.getStateDelayMs());
        gameMasterRoutine(gameConfig);
    }

    public void continueGame(HostNetworkInfo lastMasterNetworkInfo, HostNetworkInfo selfHostNetworkInfo) {
        this.game = new Game(lastGameState, curGameInfo.getConfig(), lastMasterNetworkInfo, selfHostNetworkInfo, curGameInfo.getGameName());
        StateOrder.setStateOrder(lastGameState.getStateOrder() + 1);
        log.info("Create copy of game");
        gameMasterRoutine(curGameInfo.getConfig());
    }

    public void updatePlayersList() {
        lastGameState.getPlayers().getPlayersList()
                        .forEach(player -> {
                            if (player.hasIpAddress()) {
                                try {
                                    HostNetworkInfo hostNetworkInfo = new HostNetworkInfo(InetAddress.getByName(player.getIpAddress()), player.getPort());
                                    networkController.getNetworkStorage().addPlayer(hostNetworkInfo, player.getRole());
                                } catch (UnknownHostException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
    }

    public void steerMessage(SnakesProto.GameMessage.SteerMsg steerMsg, HostNetworkInfo hostNetworkInfo) {
        game.addMove(hostNetworkInfo, steerMsg.getDirection());
    }

    public int joinMessage(SnakesProto.GameMessage.JoinMsg joinMsg, HostNetworkInfo hostNetworkInfo, SnakesProto.NodeRole role) {
        log.info(joinMsg.getPlayerName() + " " + hostNetworkInfo);
        return game.addPlayer(joinMsg, hostNetworkInfo, role);
    }

    public void addMove(InetAddress ip, int port, SnakesProto.Direction move) {
        if (role == SnakesProto.NodeRole.MASTER) {
            game.addMove(new HostNetworkInfo(ip, port), move);
        } else {
            networkController.addMessage(curGameInfo.getGameName(), GameMessageCreator.initGameMessage(
                                                                        SnakesProto.GameMessage.SteerMsg
                                                                        .newBuilder()
                                                                        .setDirection(move)
                                                                        .build()));
        }
    }

    public void startAnnouncementMsg(SnakesProto.GameMessage.AnnouncementMsg announcementMsg) {
        networkController.sendAnnouncementMsg(announcementMsg);
    }

    @Override
    public void updateGameState(SnakesProto.GameState gameState) {
        if (gameState.getStateOrder() > curGameStateOrder) {
            curGameStateOrder = gameState.getStateOrder();
        } else {
            return;
        }

        if (role == SnakesProto.NodeRole.MASTER) {
            networkController.addGameStateMessage(gameState);
        }
        view.updateView(gameState);
        this.lastGameState = gameState;
    }

    @Override
    public void updateAnnouncementMsg(SnakesProto.GameMessage.AnnouncementMsg announcementMsg) {
        networkController.updateAnnouncementMsg(announcementMsg);
    }


    public void openJoinGame(HostNetworkInfo hostNetworkInfo, int selfPlayerId) {
        this.selfPlayerId = selfPlayerId;
        Platform.runLater(() -> {
            view.closeJoinStage();
            view.openGameWindow(curGameInfo.getConfig(), this);
            networkController.startMessageScheduler(curGameInfo.getConfig().getStateDelayMs());
        });
    }

    public void updatePlayerRole(HostNetworkInfo hostNetworkInfo, SnakesProto.NodeRole newRole) {
        game.updatePlayerRole(hostNetworkInfo, newRole);
    }

    public void handleAfkPlayer(HostNetworkInfo hostNetworkInfo) {
        game.handleAfkPlayer(hostNetworkInfo);
    }

    public void addJoinMessage(SnakesProto.GameAnnouncement gameInfo, SnakesProto.GameMessage.JoinMsg joinMsg) {
        this.curGameInfo = gameInfo;
        this.playerName = joinMsg.getPlayerName();
        log.info("Send joinMsg to " + gameInfo.getGameName());
        networkController.setSelfRole(joinMsg.getRequestedRole());
        networkController.setMasterInfo(gameInfo.getGameName());
        networkController.startPing(curGameInfo.getConfig().getStateDelayMs());
        networkController.addMessage(gameInfo.getGameName(), GameMessageCreator.initGameMessage(joinMsg));
    }

    public SnakesProto.NodeRole checkDeputy(SnakesProto.NodeRole role) {
        return networkController.checkDeputy(role);
    }

    @Override
    public void addObserver(GameListObserver observer) {
        gameListObservers.add(observer);
    }

    @Override
    public void notifyGameListObservers(final List<SnakesProto.GameMessage.AnnouncementMsg> gamesList) {
        gameListObservers.forEach(observer -> {
            observer.updateGamesList(gamesList);
        });
    }
}
