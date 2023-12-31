package ru.nsu.ccfit.tihomolov.task3b.network.udp;

import lombok.extern.slf4j.Slf4j;
import ru.nsu.ccfit.tihomolov.task3b.game.controller.GameController;
import ru.nsu.ccfit.tihomolov.task3b.game.model.GameMessageCreator;
import ru.nsu.ccfit.tihomolov.task3b.network.storage.HostNetworkInfo;
import ru.nsu.ccfit.tihomolov.task3b.network.storage.Message;
import ru.nsu.ccfit.tihomolov.task3b.network.storage.NetworkStorage;
import ru.nsu.ccfit.tihomolov.task3b.network.storage.NodeInfo;
import ru.nsu.ccfit.tihomolov.task3b.proto.SnakesProto;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;


@Slf4j
public class PlayersScheduler implements Runnable {
    private final NetworkStorage networkStorage;
    private final GameController gameController;
    private final long delay;
    private final long afkDelay;
    private final LinkedList<HostNetworkInfo> playersToRemove = new LinkedList<>();

    public PlayersScheduler(NetworkStorage networkStorage, long delay, GameController gameController) {
        this.networkStorage = networkStorage;
        this.delay = delay / 5;
        this.afkDelay = this.delay * 8;
        this.gameController = gameController;
    }

    @Override
    public void run() {
        while (networkStorage.getLastSendTime() == null ||
                networkStorage.getMainRoles().getMaster() == null ||
                networkStorage.getMainRoles().getDeputy() == null) {
            //log.info(networkStorage.getLastSendTime() + " " + networkStorage.getMainRoles().getMaster());
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        while (!Thread.currentThread().isInterrupted()) {
            playersToRemove.clear();
            if (networkStorage.getMainRoles().getSelf() != SnakesProto.NodeRole.MASTER &&
                    System.currentTimeMillis() - networkStorage.getLastSendTime() > delay) {
                SnakesProto.GameMessage gameMessage = GameMessageCreator.initGameMessage(SnakesProto.GameMessage.PingMsg.newBuilder().build());
                networkStorage.addToMessageToSend(new Message(gameMessage, networkStorage.getMainRoles().getMaster()));
            }
            networkStorage.getPlayersMap()
                    .forEach((key, value) -> {
                        Long lastMessageTime = value.getTime();
                        if (lastMessageTime != null && System.currentTimeMillis() - lastMessageTime > afkDelay) {
                            //log.info(System.currentTimeMillis() + " - " + value.getTime() + " > " + afkDelay);
                            handleAfk(value, key);
                        }
                    });
            playersToRemove.forEach(afkPlayer -> {
                networkStorage.removePlayer(afkPlayer);
                if (networkStorage.getMainRoles().getSelf() == SnakesProto.NodeRole.MASTER)
                    gameController.handleAfkPlayer(afkPlayer);
            });
            if (networkStorage.getMainRoles().getDeputy() == null) {
                System.out.println("find new deputy");
                findNewDeputy();
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
                Thread.currentThread().interrupt();
                //throw new RuntimeException(e);
            }
        }
    }

    private void handleAfk(NodeInfo nodeInfo, HostNetworkInfo hostNetworkInfo) {
        log.info(networkStorage.getMainRoles().getSelf() + " find that " + nodeInfo.getRole() + " afk");
        if (networkStorage.getMainRoles().getSelf() == SnakesProto.NodeRole.NORMAL && nodeInfo.getRole() == SnakesProto.NodeRole.MASTER) {
            log.info("Deputy: " + networkStorage.getMainRoles().getDeputy().toString());
            networkStorage.getMainRoles().setMaster(networkStorage.getMainRoles().getDeputy());
            networkStorage.addPlayer(networkStorage.getMainRoles().getDeputy(), SnakesProto.NodeRole.MASTER);
        } else if (networkStorage.getMainRoles().getSelf() == SnakesProto.NodeRole.MASTER && nodeInfo.getRole() == SnakesProto.NodeRole.DEPUTY) {
            networkStorage.getMainRoles().setDeputy(null);
        } else if (networkStorage.getMainRoles().getSelf() == SnakesProto.NodeRole.DEPUTY && nodeInfo.getRole() == SnakesProto.NodeRole.MASTER) {
            gameController.continueGame(hostNetworkInfo, networkStorage.getMainRoles().getDeputy());
            gameController.updatePlayersList();
            findNewDeputy();
        }
        playersToRemove.add(hostNetworkInfo);
    }

    private void findNewDeputy() {
        Optional<Map.Entry<HostNetworkInfo, NodeInfo>> newDeputy =
                networkStorage.getPlayersMap().entrySet()
                        .stream()
                        .filter((entry) -> entry.getValue().getRole() == SnakesProto.NodeRole.NORMAL)
                        .findFirst();

        newDeputy.ifPresent(entry -> {
            log.info(entry.getKey().toString());
            networkStorage.getPlayerByKey(entry.getKey()).updateRole(SnakesProto.NodeRole.DEPUTY);
            gameController.updatePlayerRole(entry.getKey(), SnakesProto.NodeRole.DEPUTY);
            networkStorage.getMainRoles().setDeputy(entry.getKey());
            networkStorage.addToMessageToSend(new Message(GameMessageCreator.initGameMessage(SnakesProto.GameMessage.RoleChangeMsg
                    .newBuilder()
                    .setReceiverRole(SnakesProto.NodeRole.DEPUTY)
                    .build()), entry.getKey()));
        });
    }
}
