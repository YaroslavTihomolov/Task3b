package ru.nsu.ccfit.tihomolov.task3b;

import javafx.application.Application;
import javafx.stage.Stage;
import ru.nsu.ccfit.tihomolov.task3b.game.controller.GameController;
import ru.nsu.ccfit.tihomolov.task3b.network.NetworkController;
import ru.nsu.ccfit.tihomolov.task3b.game.ui.View;

import java.io.IOException;


public class Main extends Application {
    private static String[] args;

    @Override
    public void start(Stage stage) throws IOException {
        View view = new View(stage);
        GameController controller = new GameController(view);
        view.setGameController(controller);
        view.openMenu();
        NetworkController networkController = new NetworkController(args[0], Integer.parseInt(args[1]), controller);
    }

    public static void main(String[] args) {
        Main.args = args;
        launch();
    }
}