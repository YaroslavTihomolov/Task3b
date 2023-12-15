package ru.nsu.ccfit.tihomolov.task3b.game.model;

import ru.nsu.ccfit.tihomolov.task3b.proto.SnakesProto;

public class Player {
    private final static int START_SCORE = 0;
    public static SnakesProto.GamePlayer initMaster(int playerId, String name) {
        return SnakesProto.GamePlayer.newBuilder()
                .setName(name)
                .setId(playerId)
                .setRole(SnakesProto.NodeRole.MASTER)
                .setType(SnakesProto.PlayerType.HUMAN)
                .setScore(START_SCORE)
                .build();
    }

    public static SnakesProto.GamePlayer updateScore(SnakesProto.GamePlayer oldState, int addScore) {
        return SnakesProto.GamePlayer.newBuilder()
                .setId(oldState.getId())
                .setName(oldState.getName())
                .setRole(oldState.getRole())
                .setType(oldState.getType())
                .setScore(oldState.getScore() + addScore)
                .setPort(oldState.getPort())
                .setIpAddress(oldState.getIpAddress())
                .build();
    }

    public static SnakesProto.GamePlayer updateMasterScore(SnakesProto.GamePlayer oldState, int addScore) {
        return SnakesProto.GamePlayer.newBuilder()
                .setId(oldState.getId())
                .setName(oldState.getName())
                .setRole(oldState.getRole())
                .setType(oldState.getType())
                .setScore(oldState.getScore() + addScore)
                .build();
    }

    public static SnakesProto.GamePlayer init(int playerId, SnakesProto.PlayerType type, String name,
                                              SnakesProto.NodeRole role, String ip, int port) {
        return SnakesProto.GamePlayer.newBuilder()
                .setName(name)
                .setIpAddress(ip)
                .setPort(port)
                .setId(playerId)
                .setRole(role)
                .setType(type)
                .setScore(START_SCORE)
                .build();
    }

    public static SnakesProto.GamePlayer updateRole(SnakesProto.GamePlayer oldState, SnakesProto.NodeRole role) {
        return SnakesProto.GamePlayer.newBuilder()
                .setName(oldState.getName())
                .setIpAddress(oldState.getIpAddress())
                .setPort(oldState.getPort())
                .setId(oldState.getId())
                .setRole(role)
                .setType(oldState.getType())
                .setScore(oldState.getScore())
                .build();
    }
}
