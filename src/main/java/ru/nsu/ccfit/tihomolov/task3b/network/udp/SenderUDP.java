package ru.nsu.ccfit.tihomolov.task3b.network.udp;

import lombok.extern.slf4j.Slf4j;
import ru.nsu.ccfit.tihomolov.task3b.network.storage.NetworkStorage;
import java.io.IOException;
import java.net.DatagramSocket;


@Slf4j
public class SenderUDP implements Runnable {
    private final DatagramSocket datagramSocket;
    private final NetworkStorage networkStorage;

    public SenderUDP(DatagramSocket datagramSocket, NetworkStorage networkStorage) {
        this.networkStorage = networkStorage;
        this.datagramSocket = datagramSocket;
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (datagramSocket) {
                for (var message : networkStorage.getMessagesForSend()) {
                    try {
                        if (message.getSendTime() != null) continue;
                        datagramSocket.send(message.getDatagramPacket());
                        //log.info(message.getType() + " " + message.getMsgSeq());
                        message.setSendTime(System.currentTimeMillis());
                        networkStorage.setLastSendTime(System.currentTimeMillis());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                datagramSocket.notifyAll();
                try {
                    datagramSocket.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
