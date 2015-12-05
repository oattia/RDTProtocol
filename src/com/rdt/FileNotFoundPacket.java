package com.rdt;

import java.net.DatagramPacket;

public class FileNotFoundPacket extends Packet {

    String fileName;

    public FileNotFoundPacket(String requestedFilePath, long seqNo){
        this.packetType = T_FILE_NOT_FND;
        this.fileName = requestedFilePath;
        this.chunkData = getBytes(fileName);
        this.chunkLength = this.chunkData.length;
        this.seqNo = seqNo;
    }

    public FileNotFoundPacket(DatagramPacket packet){
        super(packet);
        fileName = getString(chunkData);
    }

}
