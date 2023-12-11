package ru.nsu.ccfit.tihomolov.task3b.network.udp;

import lombok.extern.slf4j.Slf4j;
import ru.nsu.ccfit.tihomolov.task3b.game.controller.GameController;
import ru.nsu.ccfit.tihomolov.task3b.game.model.GameMessageCreator;
import ru.nsu.ccfit.tihomolov.task3b.network.storage.HostNetworkInfo;
import ru.nsu.ccfit.tihomolov.task3b.network.storage.Message;
import ru.nsu.ccfit.tihomolov.task3b.network.storage.NetworkStorage;
import ru.nsu.ccfit.tihomolov.task3b.network.storage.NodeInfo;
import ru.nsu.ccfit.tihomolov.task3b.snakes.proto.SnakesProto;

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
        this.delay = delay / 10;
        this.afkDelay = this.delay * 8;
        this.gameController = gameController;
    }

    @Override
    public void run() {
        waitForActivate();

        while (!Thread.currentThread().isInterrupted()) {
            playersToRemove.clear();
            checkForAfk();
            checkDeputy();
            removeAfkPlayers();

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
                Thread.currentThread().interrupt();
            }
        }

    }

    private void checkDeputy() {
        if (networkStorage.getMainRoles().getDeputy() == null && networkStorage.getPlayersCount() != 0) {
            findNewDeputy();
        }
    }

    private void checkForAfk() {
        networkStorage.getPlayersMap()
                .forEach((key, value) -> {
                    Long lastMessageTime = value.getTime();
                    if (lastMessageTime != null && System.currentTimeMillis() - lastMessageTime > afkDelay) {
                        handleAfk(value, key);
                    }
                });
    }

    private void waitForActivate() {
        while (networkStorage.getMainRoles().getMaster() == null) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

    private void removeAfkPlayers() {
        playersToRemove.forEach(afkPlayer -> {
            networkStorage.removePlayer(afkPlayer);
            if (networkStorage.getMainRoles().getSelf() == SnakesProto.NodeRole.MASTER) gameController.handleAfkPlayer(afkPlayer);
        });
    }

    private void handleAfk(NodeInfo nodeInfo, HostNetworkInfo hostNetworkInfo) {
        log.info(networkStorage.getMainRoles().getSelf() + " find that " + nodeInfo.getRole() + " afk");

        if (networkStorage.getMainRoles().getSelf() == SnakesProto.NodeRole.NORMAL && nodeInfo.getRole() == SnakesProto.NodeRole.MASTER) {
            log.info("Deputy: " + networkStorage.getMainRoles().getDeputy().toString());
            networkStorage.getMainRoles().setMaster(networkStorage.getMainRoles().getDeputy());
            networkStorage.addPlayer(networkStorage.getMainRoles().getDeputy(), SnakesProto.NodeRole.MASTER);

        } else if (networkStorage.getMainRoles().getSelf() == SnakesProto.NodeRole.MASTER && nodeInfo.getRole() == SnakesProto.NodeRole.DEPUTY) {
            findNewDeputy();

        } else if (networkStorage.getMainRoles().getSelf() == SnakesProto.NodeRole.DEPUTY && nodeInfo.getRole() == SnakesProto.NodeRole.MASTER) {
            gameController.continueGame(hostNetworkInfo, networkStorage.getMainRoles().getDeputy());
            gameController.updatePlayersList();
            findNewDeputy();
        }

        playersToRemove.add(hostNetworkInfo);
    }

    private void findNewDeputy() {
        networkStorage.getMainRoles().setDeputy(null);

        Optional<Map.Entry<HostNetworkInfo, NodeInfo>> newDeputy =
                networkStorage.getPlayersMap().entrySet()
                        .stream()
                        .filter(entry -> entry.getValue().getRole() == SnakesProto.NodeRole.NORMAL)
                        .findFirst();

        newDeputy.ifPresent(entry -> {
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
