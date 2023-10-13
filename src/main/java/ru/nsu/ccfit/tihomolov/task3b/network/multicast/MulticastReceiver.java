package ru.nsu.ccfit.tihomolov.task3b.network.multicast;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.ccfit.tihomolov.task3b.network.storage.HostNetworkInfo;
import ru.nsu.ccfit.tihomolov.task3b.game.model.MainNodeInfo;
import ru.nsu.ccfit.tihomolov.task3b.snakes.proto.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Class for receive message
 */
@Slf4j
public class MulticastReceiver implements AutoCloseable {
    private final Map<String, MainNodeInfo> games;
    private final static Integer MAX_AFK_TIME = 2000;
    private final MulticastUDP multicastUDP;

    public MulticastReceiver(Map<String, MainNodeInfo> games, MulticastUDP multicastUDP) throws IOException {
        this.multicastUDP = multicastUDP;
        this.games = games;
        this.multicastUDP.joinToGroup();
    }

    /**
     * Receive message in infinity loop
     * and add to hash map new users
     * If user do not send messages 30 seconds user delete from map
     */
    public boolean receive() {
        SnakesProto.GameMessage.AnnouncementMsg announcementMsg;
        MainNodeInfo oldMessage = null;
        //log.info("Start receive");
        try {
            DatagramPacket packet = multicastUDP.receive();
            SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.parseFrom(Arrays.copyOfRange(packet.getData(), 0, packet.getLength()));
            announcementMsg = gameMessage.getAnnouncement();
            //log.info("get game: " + announcementMsg.getGames(0).getGameName());
            oldMessage = games.put(announcementMsg.getGames(0).getGameName(), new MainNodeInfo(announcementMsg, System.currentTimeMillis(),
                    new HostNetworkInfo(packet.getAddress(), packet.getPort())));
            if (oldMessage == null) {
                log.info("Get new main node ip: " + packet.getAddress().getHostAddress() + " port: " + packet.getPort());
            }
        } catch (InvalidProtocolBufferException e) {
            log.error(e.getMessage());
        }
        return oldMessage == null;
    }

    private boolean removeSocketStatus(Long lastActionTime) {
        return System.currentTimeMillis() - lastActionTime > MAX_AFK_TIME;
    }

    public boolean checkAlive() {
        boolean hasChanged = false;
        for (var entry : games.entrySet()) {
            if (removeSocketStatus(entry.getValue().getDate())) {
                hasChanged = true;
                games.remove(entry.getKey());
            }
        }
        return hasChanged;
    }

    public HostNetworkInfo getMainNodeInfo(String gameName) {
        return games.get(gameName).getHostNetworkInfo();
    }

    public List<SnakesProto.GameMessage.AnnouncementMsg> getAnnouncementMessages() {
        return games.values().parallelStream()
                .map(MainNodeInfo::getAnnouncementMsg)
                .toList();
    }

    @Override
    public void close() {
        multicastUDP.close();
    }
}
