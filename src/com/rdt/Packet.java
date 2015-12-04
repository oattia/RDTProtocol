package com.rdt;

import java.net.DatagramPacket;

public abstract class Packet {

    protected DatagramPacket datagram;
    protected int checkSum; // Should be 16 bit only
    protected int length;   // Should be 16 bit only

    public DatagramPacket getDatagramPacket() {
        return datagram;
    }

}
