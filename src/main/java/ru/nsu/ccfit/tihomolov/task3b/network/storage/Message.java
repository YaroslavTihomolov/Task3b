package ru.nsu.ccfit.tihomolov.task3b.network.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.nsu.ccfit.tihomolov.task3b.snakes.proto.SnakesProto;

import java.net.DatagramPacket;
import java.net.InetAddress;

@Getter
public class Message {
    private final long msgSeq;
    private final DatagramPacket datagramPacket;
    private final SnakesProto.GameMessage.TypeCase type;
    @Setter
    private Long sendTime = null;

    public Message(SnakesProto.GameMessage gameMessage, HostNetworkInfo hostNetworkInfo) {
        this.datagramPacket = new DatagramPacket(gameMessage.toByteArray(), gameMessage.getSerializedSize(),
                hostNetworkInfo.getIp(), hostNetworkInfo.getPort());
        this.type = gameMessage.getTypeCase();
        this.msgSeq = gameMessage.getMsgSeq();
    }

    public Message(SnakesProto.GameMessage gameMessage, InetAddress ip, int port) {
        this.datagramPacket = new DatagramPacket(gameMessage.toByteArray(), gameMessage.getSerializedSize(), ip, port);
        this.type = gameMessage.getTypeCase();
        this.msgSeq = gameMessage.getMsgSeq();
    }

    public Message(long messageSeq, SnakesProto.GameMessage gameMessage, SnakesProto.GameMessage.TypeCase type,
                   InetAddress ip, int port) {
        this.msgSeq = messageSeq;
        this.datagramPacket = new DatagramPacket(gameMessage.toByteArray(), gameMessage.getSerializedSize(), ip, port);
        this.type = type;
    }

    public Message(Message message, HostNetworkInfo hostNetworkInfo) {
        this.datagramPacket = new DatagramPacket(message.datagramPacket.getData(), message.getDatagramPacket().getLength(),
                hostNetworkInfo.getIp(), hostNetworkInfo.getPort());
        this.msgSeq = message.getMsgSeq();
        this.type = message.type;
    }
}
