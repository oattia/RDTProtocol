package com.rdt;

import java.net.DatagramPacket;

public class DataPacket extends Packet {

    protected long seqNo; // Should be 32 bit only


    public DataPacket(byte[] chunkData, int actualLen, long seqNo){
        this.chunkData = chunkData;
        this.chunkLength = actualLen;
        this.seqNo = seqNo;
        // checksum can't be computed without creating the datagramPacket
    }


    public DataPacket(DatagramPacket packet){
        byte[] data = packet.getData();

        byte[] actualLenBytes = new byte[4];
        System.arraycopy(data, POS_LENGTH, actualLenBytes, 0,  4);
        chunkLength = getInt(actualLenBytes);

        chunkData = new byte[chunkLength];
        System.arraycopy(data, PACKET_HEADER_SIZE, chunkData, 0, chunkLength);

        byte[] seqNoBytes = new byte[4];
        System.arraycopy(data, POS_SEQ_NO, seqNoBytes, 0, 4);
        seqNo = getInt(seqNoBytes);

        byte[] receivedChecksum = new byte[2];
        System.arraycopy(chunkData, POS_CHECKSUM, receivedChecksum, 0, 2);

        checkSum = computeChecksum(data, 2, PACKET_HEADER_SIZE + chunkLength);
        isCorrupted = (checkSum == getInt(receivedChecksum) );
    }


    @Override
    public DatagramPacket createDatagramPacket() {
        byte[] packetData = new byte[chunkLength + PACKET_HEADER_SIZE];

        // first 2 bytes are reserved for checksum

        byte[] actualLenBytes = getBytes(chunkLength);
        System.arraycopy(actualLenBytes, 0, packetData, POS_LENGTH, 4);

        byte[] seqNoBytes = getBytes(seqNo);
        System.arraycopy(seqNoBytes, 0, packetData, POS_SEQ_NO, 4);

        System.arraycopy(chunkData, 0, packetData, PACKET_HEADER_SIZE, chunkLength);

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