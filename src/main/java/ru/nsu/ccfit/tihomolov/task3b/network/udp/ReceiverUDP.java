package ru.nsu.ccfit.tihomolov.task3b.network.udp;

import lombok.extern.slf4j.Slf4j;
import ru.nsu.ccfit.tihomolov.task3b.game.controller.GameController;
import ru.nsu.ccfit.tihomolov.task3b.game.controller.MessageHandler;
import ru.nsu.ccfit.tihomolov.task3b.network.storage.NetworkStorage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static ru.nsu.ccfit.tihomolov.task3b.context.Context.BUFFER_SIZE;
import static ru.nsu.ccfit.tihomolov.task3b.context.Context.PERIOD;

@Slf4j
public class ReceiverUDP implements Runnable {
    private final NetworkStorage networkStorage;
    private final int MAX_THREAD_POOL_COUNT = 50;
    private final DatagramSocket datagramSocket;
    private final GameController gameController;
    private final ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_THREAD_POOL_COUNT);

    public ReceiverUDP(NetworkStorage networkStorage, DatagramSocket socket, GameController gameController) {
        this.networkStorage = networkStorage;
        this.datagramSocket = socket;
        this.gameController = gameController;
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            long timeOFStartReceive = System.currentTimeMillis();
            while (System.currentTimeMillis() - timeOFStartReceive < PERIOD) {
                receive();
            }
        }
    }

    private void receive() {
        byte[] buf = new byte[BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            datagramSocket.receive(packet);
            threadPoolExecutor.submit(new MessageHandler(gameController, packet, networkStorage));
        } catch (IOException e) {
            //log.info("Do not get messages");
        }
    }
}
