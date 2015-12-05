package com.rdt;

import java.net.DatagramPacket;

public class AckPacket extends Packet {

    public AckPacket(long seqNo){
        this.packetType = T_ACK;
        chunkLength = 2;
        chunkData = new byte[chunkLength];
        this.seqNo = seqNo;                     // seqNo represents ackNo

        for(int i=0; i<chunkLength; i++)        // fill data with specified length of bytes of ones or zeros
            chunkData[i] = (i%2==0)? ((byte)0x00): ((byte)0xff);
    }


    @Override
    public DatagramPacket createDatagramPacket() {
        byte[] packetData = new byte[chunkLength + PACKET_HEADER_SIZE];

        byte[] actualLenBytes = getBytes(chunkLength);
        System.arraycopy(actualLenBytes, 0, packetData, POS_LENGTH, 4);

        byte[] seqNoBytes = getBytes(seqNo);
        System.arraycopy(seqNoBytes, 0, packetData, POS_SEQ_NO, 4);

        System.arraycopy(chunkData, 0, packetData, PACKET_HEADER_SIZE, chunkLength);

        checkSum = computeChecksum(packetData, 2, PACKET_HEADER_SIZE + chunkLength);
        byte[] checksumBytes = getBytes(checkSum);
        System.arraycopy(checksumBytes, 0, packetData, POS_CHECKSUM, 2);

        return new DatagramPacket(packetData, packetData.length);
    }

    public long getAckNo() {
        return seqNo;
    }
}
