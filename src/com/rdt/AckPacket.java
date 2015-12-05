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

    public AckPacket(DatagramPacket packet){
        super(packet);
    }

    public long getAckNo() {
        return seqNo;
    }
}
