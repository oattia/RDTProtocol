package com.rdt;

import java.net.DatagramPacket;

public class FileNotFoundPacket extends Packet {

    /*public FileNotFoundPacket(String requestedFilePath){

    }*/

    public FileNotFoundPacket(DatagramPacket packet){

    }

    @Override
    public DatagramPacket createDatagramPacket() {
        return null;
    }
}
