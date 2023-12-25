package ru.nsu.ccfit.tihomolov.task3b.game.controller;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.ccfit.tihomolov.task3b.game.model.GameMessageCreator;
import ru.nsu.ccfit.tihomolov.task3b.network.storage.HostNetworkInfo;
import ru.nsu.ccfit.tihomolov.task3b.network.storage.Message;
import ru.nsu.ccfit.tihomolov.task3b.network.storage.NetworkStorage;
import ru.nsu.ccfit.tihomolov.task3b.network.storage.NodeInfo;
import ru.nsu.ccfit.tihomolov.task3b.proto.SnakesProto;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
public class MessageHandler implements Runnable {
    private final DatagramPacket packet;
    private final GameController controller;
    private final NetworkStorage networkStorage;
    private static final SnakesProto.GameMessage.AckMsg ack = SnakesProto.GameMessage.AckMsg.newBuilder().build();
    private static final SnakesProto.GameMessage gameAck = GameMessageCreator.initGameMessage(SnakesProto.GameMessage.AckMsg.newBuilder().build());
    private static final SnakesProto.GameMessage error = GameMessageCreator.initGameMessage(SnakesProto.GameMessage.ErrorMsg.newBuilder().setErrorMessage("").build());

    public MessageHandler(GameController controller, DatagramPacket packet, NetworkStorage networkStorage) {
        //log.info("Get message");
        this.packet = packet;
        this.controller = controller;
        this.networkStorage = networkStorage;
    }

    private void sendAnswer(int playerId, SnakesProto.GameMessage gameMessage) {
        if (playerId > 0) {
            System.out.println("PlayerID: " + playerId);
            networkStorage.addToMessageToSend(new Message(gameMessage.getMsgSeq(),
                    GameMessageCreator.initGameMessage(ack, playerId),
                    SnakesProto.GameMessage.TypeCase.ACK, packet.getAddress(), packet.getPort()));
        } else {
            networkStorage.addToMessageToSend(new Message(gameMessage.getMsgSeq(),
                    error, SnakesProto.GameMessage.TypeCase.ERROR, packet.getAddress(), packet.getPort()));
        }
    }

    private void sendAckMessage(SnakesProto.GameMessage gameMessage) {
        networkStorage.addToMessageToSend(new Message(gameMessage.getMsgSeq(),
                gameAck,
                SnakesProto.GameMessage.TypeCase.ACK, packet.getAddress(), packet.getPort()));
    }

    @Override
    public void run() {
        try {
            HostNetworkInfo hostNetworkInfo = new HostNetworkInfo(packet.getAddress(), packet.getPort());
            NodeInfo senderInfo = networkStorage.getPlayerByKey(hostNetworkInfo);
            if (senderInfo != null) senderInfo.updateTime();
            SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.parseFrom(Arrays.copyOfRange(packet.getData(), 0, packet.getLength()));
            //log.info(gameMessage.toString() + " " + System.currentTimeMillis());
            switch (gameMessage.getTypeCase()) {
                case PING, ERROR -> sendAckMessage(gameMessage);
                case ACK -> handleAck(gameMessage, hostNetworkInfo);
                case STEER -> handleSteer(gameMessage, hostNetworkInfo);
                case STATE -> stateHandler(gameMessage, hostNetworkInfo);
                case JOIN -> joinHandler(gameMessage, hostNetworkInfo);
                case ROLE_CHANGE -> handleChangeRole(gameMessage, hostNetworkInfo);
                case DISCOVER -> {
                }
                case TYPE_NOT_SET -> throw new RuntimeException("no such command");
            }

        } catch (InvalidProtocolBufferException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void addToPlayersIfJoin(int playerId, SnakesProto.NodeRole role) {
        if (playerId > 0) {
            networkStorage.addPlayer(new HostNetworkInfo(packet.getAddress(), packet.getPort()), role);
        }
    }

    private void stateHandler(SnakesProto.GameMessage gameMessage, HostNetworkInfo hostNetworkInfo) {
        sendAckMessage(gameMessage);
        SnakesProto.GameState gameState = gameMessage.getState().getState();
        controller.updateGameState(gameState);

        Optional<SnakesProto.GamePlayer> deputy = gameState.getPlayers().getPlayersList().stream()
                .filter(player -> player.getRole() == SnakesProto.NodeRole.DEPUTY)
                .findFirst();
        deputy.ifPresent(gamePlayer -> {
            //log.info(gamePlayer.getName() + " " + gamePlayer.getRole());
            try {
                String ip = HostNetworkInfo.handleIp(gamePlayer.getIpAddress());
                HostNetworkInfo deputyHostNetworkInfo = new HostNetworkInfo(InetAddress.getByName(ip), gamePlayer.getPort());
                //log.info("set deputy");
                networkStorage.getMainRoles().setDeputy(deputyHostNetworkInfo);
                //log.info("Set deputy " + gamePlayer.getName() + " " + gamePlayer.getRole());
                networkStorage.updateMainRoles(hostNetworkInfo, deputyHostNetworkInfo);
            } catch (UnknownHostException e) {
                log.error(e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    private void joinHandler(SnakesProto.GameMessage gameMessage, HostNetworkInfo hostNetworkInfo) {
        SnakesProto.GameMessage.JoinMsg joinMsg = gameMessage.getJoin();
        var time = networkStorage.getJoinAttempts().get(hostNetworkInfo);
        if (time != null && System.currentTimeMillis() - time < 1000) {
            sendAnswer(-1, gameMessage);
            networkStorage.getJoinAttempts().put(hostNetworkInfo, System.currentTimeMillis());
            return;
        }
        networkStorage.getJoinAttempts().put(hostNetworkInfo, System.currentTimeMillis());
        SnakesProto.NodeRole realRole = controller.checkDeputy(joinMsg.getRequestedRole());
        int playerId = controller.joinMessage(joinMsg, hostNetworkInfo, realRole);
        sendAnswer(playerId, gameMessage);
        log.info("Real role: " + realRole);
        addToPlayersIfJoin(playerId, realRole);

        if (realRole == SnakesProto.NodeRole.DEPUTY) {
            networkStorage.getMainRoles().setDeputy(hostNetworkInfo);
            networkStorage.addToMessageToSend(new Message(GameMessageCreator.initGameMessage(SnakesProto.GameMessage.RoleChangeMsg.newBuilder()
                    .setReceiverRole(SnakesProto.NodeRole.DEPUTY)
                    .build()
            ), hostNetworkInfo));
        }
    }

    private void handleChangeRole(SnakesProto.GameMessage gameMessage, HostNetworkInfo hostNetworkInfo) {
        sendAckMessage(gameMessage);
        SnakesProto.GameMessage.RoleChangeMsg roleChangeMsg = gameMessage.getRoleChange();
        if (roleChangeMsg.hasReceiverRole()) {
            networkStorage.getMainRoles().setSelf(roleChangeMsg.getReceiverRole());
        } else if (roleChangeMsg.getSenderRole() == SnakesProto.NodeRole.DEPUTY) {
            networkStorage.getMainRoles().setDeputy(hostNetworkInfo);
        } else {
            networkStorage.getMainRoles().setMaster(hostNetworkInfo);
        }
    }


    private void handleAck(SnakesProto.GameMessage gameMessage, HostNetworkInfo hostNetworkInfo) {
        if (gameMessage.getReceiverId() > 0) {
            controller.openJoinGame(hostNetworkInfo, gameMessage.getReceiverId());
            networkStorage.addPlayer(new HostNetworkInfo(packet.getAddress(), packet.getPort()), SnakesProto.NodeRole.MASTER);
        }
        networkStorage.removeFromMessageToSend(gameMessage.getMsgSeq());
    }

    private void handleSteer(SnakesProto.GameMessage gameMessage, HostNetworkInfo hostNetworkInfo) {
        sendAckMessage(gameMessage);
        controller.steerMessage(gameMessage.getSteer(), hostNetworkInfo);
    }
}
