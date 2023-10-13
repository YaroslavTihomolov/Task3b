package ru.nsu.ccfit.tihomolov.task3b.game.model;

import ru.nsu.ccfit.tihomolov.task3b.game.controller.Observer;

public interface Observable {
    void addObserver(Observer observer);
    void notifyGameState();
    void updateUpdateAnnouncementMsg();
}
