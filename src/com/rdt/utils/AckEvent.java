package com.rdt.utils;

import com.rdt.AckPacket;

import java.net.DatagramPacket;

public class AckEvent implements Event {

    private AckPacket pkt;

    public AckEvent(DatagramPacket ackPkt) {
        pkt = new AckPacket(ackPkt);
    }

    public long getAckNo() {
        return pkt.getAckNo();
    }
}
