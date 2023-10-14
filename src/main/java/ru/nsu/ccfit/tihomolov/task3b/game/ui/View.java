package ru.nsu.ccfit.tihomolov.task3b.game.ui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import ru.nsu.ccfit.tihomolov.task3b.game.controller.GameWindowController;
import ru.nsu.ccfit.tihomolov.task3b.game.controller.JoinController;
import ru.nsu.ccfit.tihomolov.task3b.game.controller.MenuController;
import ru.nsu.ccfit.tihomolov.task3b.game.controller.GameController;
import ru.nsu.ccfit.tihomolov.task3b.snakes.proto.SnakesProto;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;


public class View {
    private volatile GraphicsContext gc;
    private static final String PATH_TO_APPLE_IMAGE = "images/ic_apple.png";
    private static final String PATH_TO_GAME_MENU = "src/main/resources/fxml/menu.fxml";
    private static final String PATH_TO_GAME_WINDOW = "src/main/resources/fxml/gameWindow.fxml";
    private static final String PATH_TO_JOIN_MENU = "src/main/resources/fxml/join.fxml";
    @Setter
    private GameController gameController;
    private static final Color DARK_GREEN = Color.web("AAD751");
    private static final Color GREEN = Color.web("A2D149");
    private static final Color SNALE_COLOR = Color.web("4674E9");
    private static final int CANVAS_WIDTH = 600;
    private static final int CANVAS_HEIGHT = 600;
    private int width;
    private int height;
    private int rectangleWidth;
    private int rectangleHeight;
    private static final Image apple = new Image(PATH_TO_APPLE_IMAGE);
    private final Stage stage;
    private Stage secondStage;
    private Pane rootGameWindow;
    private Pane rootJoinWindow;
    private Pane rootMenuWindow;
    @Getter
    private MenuController menuController;
    private GameWindowController gameWindowController;
    private final Canvas fieldCanvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);

    public View(Stage stage) {
        stage.setResizable(false);
        this.stage = stage;
    }

    public void openMenu() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        File file = new File(PATH_TO_GAME_MENU);
        fxmlLoader.setLocation(file.toURI().toURL());
        rootMenuWindow = fxmlLoader.load();
        menuController = fxmlLoader.getController();
        gameController.addObserver(menuController);
        menuController.setGameController(gameController);
        Scene scene = new Scene(rootMenuWindow);
        stage.setScene(scene);
        stage.show();
    }

    public JoinController createJoinWindow() {
        secondStage = new Stage();
        secondStage.setTitle("Join");
        FXMLLoader fxmlLoader = new FXMLLoader();
        File file = new File(PATH_TO_JOIN_MENU);
        try {
            fxmlLoader.setLocation(file.toURI().toURL());
            rootJoinWindow = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Scene scene = new Scene(rootJoinWindow);
        secondStage.setResizable(false);
        secondStage.setScene(scene);
        secondStage.show();
        return fxmlLoader.getController();
    }

    public void closeJoinStage() {
        secondStage.close();
    }

    public void openGameWindow(SnakesProto.GameConfig gameConfig, GameController gameController) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        File file = new File(PATH_TO_GAME_WINDOW);

        this.height = gameConfig.getHeight();
        this.width = gameConfig.getWidth();
        this.rectangleWidth = CANVAS_WIDTH / width;
        this.rectangleHeight = CANVAS_HEIGHT / height;

        gc = fieldCanvas.getGraphicsContext2D();
        stage.setResizable(false);
        stage.setTitle("Snake");


        try {
            fxmlLoader.setLocation(file.toURI().toURL());
            rootGameWindow = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        gameWindowController = fxmlLoader.getController();
        gameWindowController.setGameController(gameController);

        gameController.addObserver(gameWindowController);

        gameWindowController.setCanvas(fieldCanvas);

        rootGameWindow.getChildren().add(fieldCanvas);
        Scene scene = getScene(gameController, rootGameWindow, fieldCanvas);

        stage.setScene(scene);
        stage.show();
        fieldCanvas.requestFocus();

        drawBackground(gc);
    }

    private Scene getScene(GameController gameController, Pane root, Canvas canvas) {
        Scene scene = new Scene(root);

        canvas.setOnKeyPressed((event -> {
            try {
                KeyCode code = event.getCode();
                if (code == KeyCode.RIGHT || code == KeyCode.D) {
                    gameController.addMove(InetAddress.getLocalHost(), 0, SnakesProto.Direction.RIGHT);
                } else if (code == KeyCode.LEFT || code == KeyCode.A) {
                    gameController.addMove(InetAddress.getLocalHost(), 0, SnakesProto.Direction.LEFT);
                } else if (code == KeyCode.UP || code == KeyCode.W) {
                    gameController.addMove(InetAddress.getLocalHost(), 0, SnakesProto.Direction.UP);
                } else if (code == KeyCode.DOWN || code == KeyCode.S) {
                    gameController.addMove(InetAddress.getLocalHost(), 0, SnakesProto.Direction.DOWN);
                }
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }

        }));
        return scene;
    }


    private void drawSnake(SnakesProto.GameState.Snake snake) {
        gc.setFill(SNALE_COLOR);

        int index = 0;

        for (var point: snake.getPointsList()) {
            if (index++ == 0) {
                gc.fillRoundRect(point.getX() * rectangleWidth, point.getY() * rectangleWidth,
                        rectangleWidth - 1, rectangleHeight - 1, 35, 35);
            } else {
                gc.fillRoundRect(point.getX() * rectangleWidth, point.getY() * rectangleWidth,
                        rectangleWidth - 1, rectangleHeight - 1, 20, 20);
            }
        }
    }


    private void drawFood(List<SnakesProto.GameState.Coord> food) {
        for (var point: food) {
            gc.drawImage(apple, point.getX() * rectangleWidth, point.getY() * rectangleHeight, rectangleWidth, rectangleHeight);
        }
    }

    public void updateView(SnakesProto.GameState gameState) {
        Platform.runLater(() -> {
            fieldCanvas.requestFocus();
            drawBackground(gc);
            gameState.getSnakesList().forEach(this::drawSnake);
            drawFood(gameState.getFoodsList());
            gameWindowController.updatePlayersScore(gameState.getPlayers().getPlayersList());
        });
    }
    private void drawBackground(GraphicsContext gc) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if ((i + j) % 2 == 0) {
                    gc.setFill(DARK_GREEN);
                } else {
                    gc.setFill(GREEN);
                }
                gc.fillRect(i * rectangleWidth, j * rectangleHeight, rectangleWidth, rectangleHeight);
            }
        }
    }
}
