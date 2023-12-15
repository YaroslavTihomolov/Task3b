package ru.nsu.ccfit.tihomolov.task3b.network.multicast;


import lombok.extern.slf4j.Slf4j;
import ru.nsu.ccfit.tihomolov.task3b.proto.SnakesProto;

import java.io.IOException;
import java.net.*;

import static ru.nsu.ccfit.tihomolov.task3b.context.Context.BUFFER_SIZE;

/**
 * Class for work with multicast
 * Create multicast socket
 * Can join Group
 * Can send and receive message
 */
@Slf4j
public class MulticastUDP {
    private final byte[] buf;
    private final MulticastSocket multicastSocket;
    private final static Integer RECEIVE_TIMEOUT = 5000;

    private final Integer port;
    private final String ip;

    public MulticastUDP(String ip, int port) throws IOException {
        this.ip = ip;
        this.port = port;
        multicastSocket = new MulticastSocket(port);
        buf = new byte[BUFFER_SIZE];
    }

    public void joinToGroup() {
        try {
            SocketAddress mcastaddr = new java.net.InetSocketAddress(InetAddress.getByName(ip), port);
            multicastSocket.joinGroup(mcastaddr, null);
            //log.info("Join to group with port: " + port + " ip: " + ip);
        } catch (IOException e) {
            //log.error(e.toString());
        }
    }

    public void sendMessage(SnakesProto.GameMessage.AnnouncementMsg message) {
        //log.info("send to " + ip + " " + port);
        try {
            DatagramPacket packet = new DatagramPacket(message.toByteArray(), message.getSerializedSize(), InetAddress.getByName(ip), port);
            multicastSocket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public DatagramPacket receive() {
        DatagramPacket packet = new DatagramPacket(this.buf, BUFFER_SIZE);
        try {
            multicastSocket.receive(packet);
            //log.info("Get packet from: " + packet.getAddress().toString() + " " + packet.getPort());
        } catch (IOException exception) {
            log.error(exception.toString());
        }
        return packet;
    }

    private void leaveGroup() {
        try {
            multicastSocket.leaveGroup(InetAddress.getByName(this.ip));
            multicastSocket.close();
        } catch (IOException e) {
            //log.error(e.toString());
        }
    }

    public void close() {
        leaveGroup();
        multicastSocket.close();
    }
}

