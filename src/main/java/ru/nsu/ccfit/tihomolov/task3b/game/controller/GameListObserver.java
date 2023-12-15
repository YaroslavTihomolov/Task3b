package ru.nsu.ccfit.tihomolov.task3b.game.controller;

import ru.nsu.ccfit.tihomolov.task3b.proto.SnakesProto;

import java.util.List;

public interface GameListObserver {
    void updateGamesList(List<SnakesProto.GameMessage.AnnouncementMsg> gamesList);
}
