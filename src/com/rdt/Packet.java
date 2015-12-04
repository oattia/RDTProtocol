package com.rdt;

import java.net.DatagramPacket;

public abstract class Packet {

    protected int checkSum; // Should be 16 bit only
    protected int length;   // Should be 16 bit only
    protected boolean isCorrupted;
    protected byte[] chunkData;

    public abstract DatagramPacket createDatagramPacket();

}
