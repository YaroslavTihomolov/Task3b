package ru.nsu.ccfit.tihomolov.task3b.game.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.ccfit.tihomolov.task3b.snakes.proto.SnakesProto;

import java.util.HashMap;
import java.util.List;

@Slf4j
public class MenuController implements GameListObserver {
    @Setter
    private GameController gameController;
    @FXML
    public TextField gameName;
    @FXML
    private TextField name;
    @FXML
    private TextField width;
    @FXML
    private TextField height;
    @FXML
    private TextField foodCount;
    @FXML
    private TextField delay;
    @FXML
    private ListView<String> games;
    private final HashMap<String, SnakesProto.GameMessage.AnnouncementMsg> announcementMessages =  new HashMap<>();

    public void buttonHandler() {
        gameController.creatGame(SnakesProto.GameConfig.newBuilder()
                .setHeight(Integer.parseInt(height.getText()))
                .setWidth(Integer.parseInt(width.getText()))
                .setFoodStatic(Integer.parseInt(foodCount.getText()))
                .setStateDelayMs(Integer.parseInt(delay.getText()))
                .build(), name.getText(), gameName.getText());
    }

    public void updateGamesList(List<SnakesProto.GameMessage.AnnouncementMsg> gamesList) {
        GamesList.updateGames(gamesList, announcementMessages, games);
    }

    public void joinToGame(MouseEvent mouseEvent) {
        String selectItem = games.getSelectionModel().getSelectedItem();
        if (mouseEvent.getClickCount() == 2 && selectItem != null) {
            String gameName = selectItem.substring(0, selectItem.indexOf(' '));
            gameController.openJoinWindow(announcementMessages.get(gameName));
        } else {
            games.getSelectionModel().clearSelection();
        }

    }
}
