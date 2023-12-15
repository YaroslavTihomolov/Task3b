module ru.nsu.ccfit.tihomolov.task3b {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires com.google.protobuf;
    requires org.slf4j;
    requires lombok;

    opens ru.nsu.ccfit.tihomolov.task3b to javafx.fxml;
    exports ru.nsu.ccfit.tihomolov.task3b;
    opens ru.nsu.ccfit.tihomolov.task3b.proto;
    exports ru.nsu.ccfit.tihomolov.task3b.proto;
    opens ru.nsu.ccfit.tihomolov.task3b.game.controller to javafx.fxml;
    exports ru.nsu.ccfit.tihomolov.task3b.game.controller;
    exports ru.nsu.ccfit.tihomolov.task3b.network;
    opens ru.nsu.ccfit.tihomolov.task3b.network to javafx.fxml;
    exports ru.nsu.ccfit.tihomolov.task3b.network.storage;
    opens ru.nsu.ccfit.tihomolov.task3b.network.storage to javafx.fxml;
}