package com.rdt.utils;

import com.rdt.AckPacket;
import java.net.DatagramPacket;

public class AckEvent implements Event {

    private AckPacket pkt;
    private long timestamp;

    public AckEvent(DatagramPacket ackPkt, long ts) {
        this.pkt = new AckPacket(ackPkt);
        this.timestamp = ts;
    }

    public long getAckNo() {
        return pkt.getAckNo();
    }
}
