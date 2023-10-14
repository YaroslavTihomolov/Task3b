package ru.nsu.ccfit.tihomolov.task3b.network.udp;

import lombok.extern.slf4j.Slf4j;
import ru.nsu.ccfit.tihomolov.task3b.game.controller.GameController;
import ru.nsu.ccfit.tihomolov.task3b.network.storage.NetworkStorage;
import java.net.DatagramSocket;
import java.net.SocketException;


@Slf4j
public class ServiceUDP {
    private final static int TIMEOUT_SOCKET = 5;
    private final SenderUDP senderUDP;
    private final ReceiverUDP receiverUDP;
    private Thread senderThread;
    private Thread recieverThread;

    public ServiceUDP(NetworkStorage networkStorage, GameController gameController) throws SocketException {
        DatagramSocket datagramSocket = new DatagramSocket();
        this.receiverUDP = new ReceiverUDP(networkStorage, datagramSocket, gameController);
        this.senderUDP = new SenderUDP(datagramSocket, networkStorage);
        datagramSocket.setSoTimeout(TIMEOUT_SOCKET);
    }

    public void start() {
        senderThread = new Thread(senderUDP);
        senderThread.start();

        recieverThread = new Thread(receiverUDP);
        recieverThread.start();
    }

    public void stop() {
        senderThread.interrupt();
        recieverThread.interrupt();
    }
}

