package ru.nsu.ccfit.tihomolov.task3b.network.storage;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.nsu.ccfit.tihomolov.task3b.snakes.proto.SnakesProto;

import java.util.Iterator;

import static java.lang.Thread.sleep;
import static ru.nsu.ccfit.tihomolov.task3b.context.Context.PERIOD;

@Slf4j
public class SentMessageScheduler implements Runnable {
    @Setter
    private Integer delay;
    private static final int GAME_DELAY_TO_ACK_MESSAGE_DELAY = 10;
    private NetworkStorage networkStorage;

    public SentMessageScheduler(NetworkStorage networkStorage, Integer delay) {
        this.networkStorage = networkStorage;
        this.delay = delay / GAME_DELAY_TO_ACK_MESSAGE_DELAY;
        System.out.println("Create");
    }

    @Override
    public void run() {
        if (delay == null) {
            log.error("Delay do not set");
            return;
        }
        while (!Thread.currentThread().isInterrupted()) {
            Iterator<Message> iterator = networkStorage.getMessagesForSend().iterator();
            while (iterator.hasNext()) {
                Message message = iterator.next();
                if (message.getSendTime() == null) continue;
                if (message.getType() == SnakesProto.GameMessage.TypeCase.ACK ||
                        message.getType() == SnakesProto.GameMessage.TypeCase.ANNOUNCEMENT ||
                        message.getType() == SnakesProto.GameMessage.TypeCase.DISCOVER) {
                    iterator.remove();
                } else if (System.currentTimeMillis() - message.getSendTime() > delay) {
                    if (networkStorage.getMainRoles().getSelf() == SnakesProto.NodeRole.MASTER) {
                        networkStorage.addToMessageToSend(message);
                    } else {
                        networkStorage.addToMessageToSend(new Message(message, networkStorage.getMainRoles().getMaster()));
                    }
                    iterator.remove();
                }
            }
            try {
                sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
