package ru.nsu.ccfit.tihomolov.task3b.network.multicast;

import lombok.extern.slf4j.Slf4j;
import ru.nsu.ccfit.tihomolov.task3b.game.controller.GameController;
import ru.nsu.ccfit.tihomolov.task3b.game.model.MainNodeInfo;
import ru.nsu.ccfit.tihomolov.task3b.snakes.proto.SnakesProto;

import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class MulticastService {
    private final MulticastUDP multicastUDP;
    private final MulticastReceiver multicastReceiver;
    private static final long SLEEP_TIME = 1000;
    private final GameController gameController;
    private SnakesProto.GameMessage.AnnouncementMsg announcementMsg;

    public MulticastService(String ip, int port, GameController gameController, Map<String, MainNodeInfo> games) {
        this.gameController = gameController;
        try {
            this.multicastUDP = new MulticastUDP(ip, port);
            this.multicastReceiver = new MulticastReceiver(games, multicastUDP);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        handleMulticastMessage();
        checkPlayers();
    }

    public void handleMulticastMessage() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                while (true) {
                    if (multicastReceiver.receive()) {
                        gameController.notifyGameListObservers(multicastReceiver.getAnnouncementMessages());
                    }
                }
            }
        };
        timer.scheduleAtFixedRate(task, SLEEP_TIME, SLEEP_TIME);
    }

    private void checkPlayers() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (multicastReceiver.checkAlive()) {
                    gameController.notifyGameListObservers(multicastReceiver.getAnnouncementMessages());
                }
            }
        };
        timer.scheduleAtFixedRate(task, SLEEP_TIME, SLEEP_TIME);
    }
}
