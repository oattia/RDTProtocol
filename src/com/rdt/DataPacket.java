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


    public long getSeqNo() {
        return seqNo;
    }


}