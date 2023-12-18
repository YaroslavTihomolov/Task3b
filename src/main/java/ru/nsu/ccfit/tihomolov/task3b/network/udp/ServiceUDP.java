package ru.nsu.ccfit.tihomolov.task3b.network.udp;

import lombok.extern.slf4j.Slf4j;
import ru.nsu.ccfit.tihomolov.task3b.game.controller.GameController;
import ru.nsu.ccfit.tihomolov.task3b.network.storage.NetworkStorage;

import java.io.IOException;
import java.net.*;


@Slf4j
public class ServiceUDP {
    private final static int TIMEOUT_SOCKET = 5;
    private final SenderUDP senderUDP;
    private final ReceiverUDP receiverUDP;
    private Thread senderThread;
    private Thread recieverThread;

    public ServiceUDP(NetworkStorage networkStorage, GameController gameController) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket();
        SocketAddress mcastaddr = new java.net.InetSocketAddress(InetAddress.getByName("239.192.0.4"), 9192);
        datagramSocket.joinGroup(mcastaddr, null);
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

