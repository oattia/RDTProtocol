package com.rdt;

public class DataPacket extends Packet {
    private long seqNo; // Should be 32 bit only
    private byte[] data;
}
