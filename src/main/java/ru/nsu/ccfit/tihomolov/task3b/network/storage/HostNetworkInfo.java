package ru.nsu.ccfit.tihomolov.task3b.network.storage;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.InetAddress;

@AllArgsConstructor
@Data
public class HostNetworkInfo {
    private InetAddress ip;
    private int port;
}
