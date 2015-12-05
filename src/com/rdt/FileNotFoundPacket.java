package com.rdt;

import java.net.DatagramPacket;

public class FileNotFoundPacket extends Packet {

    String fileName;

    public FileNotFoundPacket(String requestedFilePath){
        this.packetType = T_FILE_NOT_FND;
        this.fileName = requestedFilePath;
        this.chunkData = getBytes(fileName);
        this.chunkLength = this.chunkData.length;
        this.seqNo = 0;
    }

    public FileNotFoundPacket(DatagramPacket packet){
        super(packet);
        fileName = getString(chunkData);
    }


    @Override
    public DatagramPacket createDatagramPacket() {
        byte[] packetData = new byte[chunkLength + PACKET_HEADER_SIZE];

        byte[] actualLenBytes = getBytes(chunkLength);
        System.arraycopy(actualLenBytes, 0, packetData, POS_LENGTH, 4);

        byte[] seqNoBytes = getBytes(seqNo);
        System.arraycopy(seqNoBytes, 0, packetData, POS_SEQ_NO, 4);

        System.arraycopy(chunkData, 0, packetData, PACKET_HEADER_SIZE, chunkLength);

        byte[] packetTypeBytes = getBytes(packetType);
        System.arraycopy(packetTypeBytes, 0, packetData, POS_PACKET_TYPE, 4);

        checkSum = computeChecksum(packetData, 2, PACKET_HEADER_SIZE + chunkLength);
        System.arraycopy(getBytes(checkSum), 0, packetData, POS_CHECKSUM, 2);

        return new DatagramPacket(packetData, packetData.length);
    }
}
