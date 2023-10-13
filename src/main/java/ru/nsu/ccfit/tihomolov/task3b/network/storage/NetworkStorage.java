package ru.nsu.ccfit.tihomolov.task3b.network.storage;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.ccfit.tihomolov.task3b.game.model.MainNodeInfo;
import ru.nsu.ccfit.tihomolov.task3b.snakes.proto.SnakesProto;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class NetworkStorage {
    @Setter
    @Getter
    private Long lastSendTime;
    @Getter
    private final Map<String, MainNodeInfo> mainNodesInfoMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<HostNetworkInfo, NodeInfo> players = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Message> messagesToSend = new ConcurrentHashMap<>();
    @Getter
    private final MainRoles mainRoles = new MainRoles();

    public void addPlayer(HostNetworkInfo hostNetworkInfo, SnakesProto.NodeRole role) {
        players.put(hostNetworkInfo, new NodeInfo(System.currentTimeMillis(), role));
    }

    public void addToMessageToSend(Message message) {
        messagesToSend.put(message.getMsgSeq(), message);
    }

    public ConcurrentHashMap.KeySetView<HostNetworkInfo, NodeInfo> playersSet() {
        return players.keySet();
    }

    public NodeInfo getPlayerByKey(HostNetworkInfo hostNetworkInfo) {
        return players.get(hostNetworkInfo);
    }

    public Collection<Message> getMessagesForSend() {
        return messagesToSend.values();
    }

    public void removeFromMessageToSend(Long msgSeq) {
        messagesToSend.remove(msgSeq);
    }

    public void removePlayer(HostNetworkInfo hostNetworkInfo) {
        players.remove(hostNetworkInfo);
    }

    public void updateMainRoles(HostNetworkInfo masterHostNetworkInfo, HostNetworkInfo deputyHostNetworkInfo) {
        mainRoles.setMaster(masterHostNetworkInfo);
        mainRoles.setDeputy(deputyHostNetworkInfo);
    }

    public ConcurrentHashMap<HostNetworkInfo, NodeInfo> getPlayersMap() {
        return players;
    }
}
