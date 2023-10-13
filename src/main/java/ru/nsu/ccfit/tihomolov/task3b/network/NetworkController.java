package ru.nsu.ccfit.tihomolov.task3b.network;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.ccfit.tihomolov.task3b.game.controller.GameController;
import ru.nsu.ccfit.tihomolov.task3b.game.model.GameMessageCreator;
import ru.nsu.ccfit.tihomolov.task3b.network.multicast.MulticastService;
import ru.nsu.ccfit.tihomolov.task3b.network.storage.HostNetworkInfo;
import ru.nsu.ccfit.tihomolov.task3b.network.storage.Message;
import ru.nsu.ccfit.tihomolov.task3b.network.storage.NetworkStorage;
import ru.nsu.ccfit.tihomolov.task3b.network.storage.SentMessageScheduler;
import ru.nsu.ccfit.tihomolov.task3b.network.udp.PlayersScheduler;
import ru.nsu.ccfit.tihomolov.task3b.network.udp.ServiceUDP;
import ru.nsu.ccfit.tihomolov.task3b.snakes.proto.SnakesProto;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class NetworkController {
    private final MulticastService multicastService;
    private static final long SLEEP_TIME = 1000;
    private final GameController gameController;
    private SnakesProto.GameMessage.AnnouncementMsg announcementMsg;
    @Getter
    private final NetworkStorage networkStorage = new NetworkStorage();
    private final InetAddress multicastIp;
    private final int multicastPort;

    public NetworkController(String ip, int port, GameController gameController) throws IOException {
        System.out.println(ip + " " + port);
        this.multicastService = new MulticastService(ip, port, gameController, networkStorage.getMainNodesInfoMap());
        this.gameController = gameController;
        this.multicastIp = InetAddress.getByName(ip);
        this.multicastPort = port;
        ServiceUDP serviceUDP = new ServiceUDP(networkStorage, gameController);
        serviceUDP.start();
        gameController.setNetworkController(this);
    }

    public void startMessageScheduler(int delay) {
        Thread thread = new Thread(new SentMessageScheduler(networkStorage, delay));
        thread.start();
    }

    /*public void startSend(SnakesProto.GameMessage.AnnouncementMsg announcementMsg) {
        multicastService.sendAnnouncementMsg(announcementMsg);
    }*/

    private void addMessage(HostNetworkInfo hostNetworkInfo, SnakesProto.GameMessage gameMessage) {
        networkStorage.addToMessageToSend(new Message(gameMessage, hostNetworkInfo));
    }

    public void sendAnnouncementMsg(SnakesProto.GameMessage.AnnouncementMsg startAnnouncementMsg) {
        Timer timer = new Timer();
        this.announcementMsg = startAnnouncementMsg;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                addMessage(new HostNetworkInfo(multicastIp, multicastPort), GameMessageCreator.initGameMessage(announcementMsg));
            }
        };
        log.info("Start multicast AnnouncementMsg");
        timer.scheduleAtFixedRate(task, SLEEP_TIME, SLEEP_TIME);
    }

    public void addGameStateMessage(SnakesProto.GameState gameState) {
        networkStorage.playersSet().forEach(player -> {
                    SnakesProto.GameMessage gameMessage = GameMessageCreator.initGameMessage(gameState);
                    networkStorage.addToMessageToSend(new Message(gameMessage, player.getIp(), player.getPort()));
                }
        );
    }

    public void updateAnnouncementMsg(SnakesProto.GameMessage.AnnouncementMsg announcementMsg) {
        this.announcementMsg = announcementMsg;
    }

    public void addMessage(String gameName, SnakesProto.GameMessage gameMessage) {
        HostNetworkInfo hostNetworkInfo = networkStorage.getMainNodesInfoMap().get(gameName).getHostNetworkInfo();
        networkStorage.addToMessageToSend(new Message(gameMessage, hostNetworkInfo));
    }

    public void setSelfRole(SnakesProto.NodeRole role) {
        networkStorage.getMainRoles().setSelf(role);
    }

    public void setMasterInfo(HostNetworkInfo hostNetworkInfo) {
        networkStorage.getMainRoles().setMaster(hostNetworkInfo);
    }

    public void setMasterInfo(String gameName) {
        HostNetworkInfo hostNetworkInfo = networkStorage.getMainNodesInfoMap().get(gameName).getHostNetworkInfo();
        networkStorage.getMainRoles().setMaster(hostNetworkInfo);
    }

    public SnakesProto.NodeRole checkDeputy(SnakesProto.NodeRole role) {
        if (role != SnakesProto.NodeRole.VIEWER && networkStorage.getMainRoles().getDeputy() == null) {
            return SnakesProto.NodeRole.DEPUTY;
        }
        return role;
    }

    public void startPing(int delay) {
        Thread thread = new Thread(new PlayersScheduler(networkStorage, delay, gameController));
        thread.start();
    }
}
