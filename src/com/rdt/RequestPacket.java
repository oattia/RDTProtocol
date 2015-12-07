package com.rdt;

import java.net.DatagramPacket;

public class RequestPacket extends Packet {

    public RequestPacket(DatagramPacket packet) {
        super(packet);
        this.packetType = T_REQUEST;
    }

    public String getFileName() {
        return getString(chunkData);
    }
}
