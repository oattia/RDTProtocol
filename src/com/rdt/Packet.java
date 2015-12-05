package com.rdt;

import java.net.DatagramPacket;

public abstract class Packet {

    /*
    * Packet structure is as follows:
    *
    * ===========================================================
    *   CHECKSUM                |   pos: 0, len: 2
    *   CHUNCK_LENGTH           |   pos: 2, len: 4
    *   SEQ_NO                  |   pos: 6, len: 4
    *   Rest of header (empty)  |   pos: 10, len: PACKET_HEADER_SIZE-10
    * -----------------------------------------------------------
    *   chunckData              |   pos: PACKET_HEADER_SIZE, len: chunkLength
    * ===========================================================
    *
    * */


    // offsets in byte array of packet
    protected static final int POS_CHECKSUM = 0;
    protected static final int POS_LENGTH = 2;
    protected static final int POS_SEQ_NO = 6;

    protected static final int PACKET_HEADER_SIZE = 20;

    protected int checkSum; // Should be 16 bit only
    protected int chunkLength;   // Should be 16 bit only
    protected boolean isCorrupted;
    protected byte[] chunkData;

    public abstract DatagramPacket createDatagramPacket();

    public byte[] getChunkData() {
        return chunkData;
    }

    public void setChunkData(byte[] chunkData) {
        this.chunkData = chunkData;
    }


    protected static int computeChecksum(byte[] data, int start, int end) {
        int sum = 0;
        for(int i=start; i<end; i+=2) {
            int num = ((((int)data[i])&0xff) << Byte.SIZE) + (((int)data[i + 1])&0xff);
            sum += num;
            if (sum >= (1 << 16))
                sum += 1;
            sum %= (1 << 16);
        }
        sum = ~sum;
        return sum;
    }

    protected static byte[] getBytes(int num) {
        byte[] bytes = new byte[Integer.BYTES];
        for(int i = 0; i < Integer.BYTES; i++)
            bytes[i] = (byte) ((num & ((0xFF) << (i << 3))) >> (i << 3));
        return bytes;
    }

    protected static byte[] getBytes(long num) {
        byte[] bytes = new byte[Long.BYTES];
        for(int i = 0; i < Long.BYTES; i++)
            bytes[i] = (byte) ((num & ((0xFF) << (i << 3))) >> (i << 3));
        return bytes;
    }

    protected static int getInt(byte[] bytes){
        int value=0;
        for(int i=0; i<bytes.length; i++){
            value = value << Byte.SIZE;
            value += (((int)bytes[i])&0xff);
        }
        return value;
    }

    protected static long getLong(byte[] bytes){
        Long value=0L;
        for(int i=0; i<bytes.length; i++){
            value = value << Byte.SIZE;
            value += (((long)bytes[i])&0xff);
        }
        return value;
    }

}
