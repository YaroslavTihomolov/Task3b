package ru.nsu.ccfit.tihomolov.task3b.game.controller;

import ru.nsu.ccfit.tihomolov.task3b.snakes.proto.SnakesProto;

import java.util.List;

public interface GameListObservable {
    void addObserver(GameListObserver observer);
    void notifyGameListObservers(final List<SnakesProto.GameMessage.AnnouncementMsg> gamesList);
}
