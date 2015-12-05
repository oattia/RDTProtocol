package com.rdt;

import java.net.DatagramPacket;

public class RequestPacket extends Packet {

    /*public RequestPacket(String requestedFilePath, int sourcePort){
        this.packetType = T_REQUEST;
    }*/

    public RequestPacket(DatagramPacket packet){

    }

    @Override
    public DatagramPacket createDatagramPacket() {
        return null;
    }
}
