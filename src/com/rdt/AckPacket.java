package com.rdt;

import java.net.DatagramPacket;

public class AckPacket extends Packet {
    private long ackNo; // Should be 32 bit only

    /*public AckPacket(){

    }*/

    public AckPacket(DatagramPacket packet){

    }

    public long getAckNo() {
        return ackNo;
    }

    @Override
    public DatagramPacket createDatagramPacket() {
        return null;
    }
}
