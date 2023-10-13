package ru.nsu.ccfit.tihomolov.task3b.utils;

import ru.nsu.ccfit.tihomolov.task3b.snakes.proto.SnakesProto;

public class JoinMessage {
    public static SnakesProto.GameMessage.JoinMsg initJoinMessage(String gameName, String playerName, SnakesProto.NodeRole role) {
        return SnakesProto.GameMessage.JoinMsg.newBuilder()
                .setGameName(gameName)
                .setPlayerName(playerName)
                .setPlayerType(SnakesProto.PlayerType.HUMAN)
                .setRequestedRole(role)
                .build();
    }
}
