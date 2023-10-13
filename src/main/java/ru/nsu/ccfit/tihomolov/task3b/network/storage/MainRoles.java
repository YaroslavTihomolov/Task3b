package ru.nsu.ccfit.tihomolov.task3b.network.storage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.nsu.ccfit.tihomolov.task3b.snakes.proto.SnakesProto;

@Data
public class MainRoles {
    private HostNetworkInfo master;

    private HostNetworkInfo deputy;

    private SnakesProto.NodeRole self;
}
