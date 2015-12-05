package com.rdt;

import java.net.DatagramPacket;

public class AckPacket extends Packet {
    private long ackNo; // Should be 32 bit only

    public AckPacket(long ackNo){
        chunkLength = 2;
        chunkData = new byte[chunkLength];
        this.ackNo = ackNo;

        for(int i=0; i<chunkLength; i++)        // fill data with specified length of bytes of ones or zeros
            chunkData[i] = (i%2==0)? ((byte)0x00): ((byte)0xff);
    }

    public AckPacket(DatagramPacket packet){
        byte[] data = packet.getData();

        byte[] actualLenBytes = new byte[4];
        System.arraycopy(data, POS_LENGTH, actualLenBytes, 0,  4);
        this.chunkLength = getInt(actualLenBytes);

        chunkData = new byte[chunkLength];
        System.arraycopy(data, PACKET_HEADER_SIZE, chunkData, 0, chunkLength);

        byte[] seqNoBytes = new byte[4];
        System.arraycopy(data, POS_SEQ_NO, seqNoBytes, 0, 4);
        ackNo = getInt(seqNoBytes);

        byte[] receivedChecksum = new byte[2];
        System.arraycopy(chunkData, POS_CHECKSUM, receivedChecksum, 0, 2);

        checkSum = computeChecksum(data, 2, PACKET_HEADER_SIZE + chunkLength);
        isCorrupted = (checkSum == getInt(receivedChecksum) );
    }

    @Override
    public DatagramPacket createDatagramPacket() {
        byte[] packetData = new byte[chunkLength + PACKET_HEADER_SIZE];

        byte[] actualLenBytes = getBytes(chunkLength);
        System.arraycopy(actualLenBytes, 0, packetData, POS_LENGTH, 4);

        byte[] ackNoBytes = getBytes(ackNo);
        System.arraycopy(ackNoBytes, 0, packetData, POS_SEQ_NO, 4);

        System.arraycopy(chunkData, 0, packetData, PACKET_HEADER_SIZE, chunkLength);

        checkSum = computeChecksum(packetData, 2, PACKET_HEADER_SIZE + chunkLength);
        byte[] checksumBytes = getBytes(checkSum);
        System.arraycopy(checksumBytes, 0, packetData, POS_CHECKSUM, 2);

        return new DatagramPacket(packetData, packetData.length);
    }

    public long getAckNo() {
        return ackNo;
    }
}
