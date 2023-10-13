package ru.nsu.ccfit.tihomolov.task3b.game.controller;

import ru.nsu.ccfit.tihomolov.task3b.snakes.proto.SnakesProto;

public interface Observer {
    void updateGameState(SnakesProto.GameState gameState);
    void updateAnnouncementMsg(SnakesProto.GameMessage.AnnouncementMsg announcementMsg);
}
