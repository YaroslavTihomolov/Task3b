package ru.nsu.ccfit.tihomolov.task3b.game.model;

import ru.nsu.ccfit.tihomolov.task3b.proto.SnakesProto;

public class GameMessageCreator {
    public static SnakesProto.GameMessage initGameMessage(SnakesProto.GameMessage.AnnouncementMsg announcementMsg) {
        return SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(MessageSequence.getMessageSequence())
                .setAnnouncement(announcementMsg)
                .build();
    }

    public static SnakesProto.GameMessage initGameMessage(SnakesProto.GameMessage.ErrorMsg errorMsg) {
        return SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(MessageSequence.getMessageSequence())
                .setError(errorMsg)
                .build();
    }

    public static SnakesProto.GameMessage initGameMessage(SnakesProto.GameState gameState) {
        return SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(MessageSequence.getMessageSequence())
                .setState(SnakesProto.GameMessage.StateMsg.newBuilder()
                        .setState(gameState)
                        .build())
                .build();
    }

    public static SnakesProto.GameMessage initGameMessage(SnakesProto.GameMessage.SteerMsg move) {
        return SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(MessageSequence.getMessageSequence())
                .setSteer(move)
                .build();
    }

    public static SnakesProto.GameMessage initGameMessage(SnakesProto.GameMessage.JoinMsg joinMessage) {
        return SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(MessageSequence.getMessageSequence())
                .setJoin(joinMessage)
                .build();
    }

    public static SnakesProto.GameMessage initGameMessage(SnakesProto.GameMessage.AckMsg ackMsg) {
        return SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(MessageSequence.getMessageSequence())
                .setAck(ackMsg)
                .build();
    }

    public static SnakesProto.GameMessage initGameMessage(SnakesProto.GameMessage.AckMsg ackMsg, int playerId) {
        return SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(MessageSequence.getMessageSequence())
                .setAck(ackMsg)
                .setReceiverId(playerId)
                .build();
    }

    public static SnakesProto.GameMessage initGameMessage(SnakesProto.GameMessage.PingMsg pingMsg) {
        return SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(MessageSequence.getMessageSequence())
                .setPing(pingMsg)
                .build();
    }

    public static SnakesProto.GameMessage initGameMessage(SnakesProto.GameMessage.RoleChangeMsg roleChangeMsg) {
        return SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(MessageSequence.getMessageSequence())
                .setRoleChange(roleChangeMsg)
                .build();
    }
}
