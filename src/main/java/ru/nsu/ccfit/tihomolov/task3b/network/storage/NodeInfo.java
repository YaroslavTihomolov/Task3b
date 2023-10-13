package ru.nsu.ccfit.tihomolov.task3b.network.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.nsu.ccfit.tihomolov.task3b.snakes.proto.SnakesProto;

@Getter
@AllArgsConstructor
public class NodeInfo {
    private Long time;

    @Setter
    private SnakesProto.NodeRole role;

    public void updateTime() {
        this.time = System.currentTimeMillis();
    }

    public void updateRole(SnakesProto.NodeRole role) {
        this.role = role;
    }
}
