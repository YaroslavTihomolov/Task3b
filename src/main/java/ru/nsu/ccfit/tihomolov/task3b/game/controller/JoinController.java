package ru.nsu.ccfit.tihomolov.task3b.game.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.ccfit.tihomolov.task3b.proto.SnakesProto;
import ru.nsu.ccfit.tihomolov.task3b.utils.JoinMessage;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class JoinController implements Initializable {
    @Setter
    private GameController gameController;
    @FXML
    public TextField playerName;
    @FXML
    public TextField errorText;
    @FXML
    public ChoiceBox<String> roles;
    @Setter
    private SnakesProto.GameMessage.AnnouncementMsg announcementMsg;

    private SnakesProto.NodeRole getRoleChoiceBox() {
        return roles.getValue().equals("Зритель") ? SnakesProto.NodeRole.VIEWER : SnakesProto.NodeRole.NORMAL;
    }

    public void printErrorText() {
        errorText.setText("Не возможно присоединится к игре");
    }

    public void joinButtonHandler() {
        log.info(playerName.getText());
        SnakesProto.GameAnnouncement gameInfo = announcementMsg.getGames(0);
        gameController.addJoinMessage(gameInfo, JoinMessage.initJoinMessage(gameInfo.getGameName(), playerName.getText(), getRoleChoiceBox()));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roles.getItems().setAll("Зритель", "Игрок");
    }
}
