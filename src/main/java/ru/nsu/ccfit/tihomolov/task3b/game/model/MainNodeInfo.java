package ru.nsu.ccfit.tihomolov.task3b.game.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.nsu.ccfit.tihomolov.task3b.network.storage.HostNetworkInfo;
import ru.nsu.ccfit.tihomolov.task3b.proto.SnakesProto;


@Getter
@AllArgsConstructor
public class MainNodeInfo {
    private SnakesProto.GameMessage.AnnouncementMsg announcementMsg;
    private long date;
    private HostNetworkInfo hostNetworkInfo;
}
