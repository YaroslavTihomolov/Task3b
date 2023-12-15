package ru.nsu.ccfit.tihomolov.task3b.game.controller;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import ru.nsu.ccfit.tihomolov.task3b.proto.SnakesProto;

import java.util.HashMap;
import java.util.List;

public abstract class GamesList {

    public static void updateGames(List<SnakesProto.GameMessage.AnnouncementMsg> gamesList,
                            HashMap<String, SnakesProto.GameMessage.AnnouncementMsg> announcementMessages, ListView<String> games) {
        List<String> curGames = gamesList.stream().map(game -> {
            announcementMessages.put(game.getGames(0).getGameName(), game);
            SnakesProto.GameAnnouncement gameAnnouncement = game.getGames(0);
            return gameAnnouncement.getGameName() + " " + gameAnnouncement.getPlayers().getPlayersCount();
        }).toList();
        Platform.runLater(() -> games.getItems().setAll(curGames));
    }
}
