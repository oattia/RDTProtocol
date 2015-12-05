package com.rdt;

import java.net.DatagramPacket;

public class DataPacket extends Packet {


    public DataPacket(byte[] chunkData, int actualLen, long seqNo){
        this.packetType = T_DATA;
        this.chunkData = chunkData;
        this.chunkLength = actualLen;
        this.seqNo = seqNo;
        // checksum can't be computed without creating the datagramPacket
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


    public long getSeqNo() {
        return seqNo;
    }


//    public static void main(String[] args) {
//        byte[] data = {(byte)0b01100110, (byte)0b01100000, (byte)0b01010101,
//                (byte)0b01010101, (byte)0b10001111, (byte)0b00001100};
//        int checksum = computeChecksum(data, 0, 6);
//        System.out.println(String.format("%h", checksum));
//    }


}