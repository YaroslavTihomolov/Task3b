package ru.nsu.ccfit.tihomolov.task3b.game.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ListView;
import ru.nsu.ccfit.tihomolov.task3b.snakes.proto.SnakesProto;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class GameWindowController implements GameListObserver {

    @FXML
    private Canvas gameWindow;

    @FXML
    private ListView<String> games;

    @FXML
    private ListView<String> score;
    private int index = 1;
    private final HashMap<String, SnakesProto.GameMessage.AnnouncementMsg> announcementMessages =  new HashMap<>();

    public void updateGamesList(List<SnakesProto.GameMessage.AnnouncementMsg> gamesList) {
        GamesList.updateGames(gamesList, announcementMessages, games);
    }


    public void setCanvas(Canvas canvas) {
        this.gameWindow = canvas;
    }


    public void updatePlayersScore(List<SnakesProto.GamePlayer> playersList) {
        List<String> playersScore = playersList.stream()
                .sorted(Comparator.comparingInt(SnakesProto.GamePlayer::getScore).reversed())
                .map(player -> {
                    String status = "";
                    switch (player.getRole()) {
                        case MASTER -> status = " [ведущий]";
                        case DEPUTY -> status = " [соведущий]";
                        case VIEWER -> status = " [зритель]";
                    }
                    return index++ + ". " + player.getName() + " " + player.getScore() + status;
                }).toList();
        index = 1;
        Platform.runLater(() -> score.getItems().setAll(playersScore));
    }
}
