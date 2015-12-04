package com.rdt;

import java.net.DatagramPacket;

public class DataPacket extends Packet {

    private long seqNo; // Should be 32 bit only

    private static final int PACKET_HEADER_SIZE = 50;

    public DataPacket(byte[] chunkData, int actualLen, long seqNo){
        this.chunkData = chunkData;
        this.length = actualLen;
        this.seqNo = seqNo;
    }

    public DataPacket(byte[] packet){
        byte[] actualLenBytes = new byte[4];
        System.arraycopy(packet, 2, actualLenBytes, 0,  4);
        length = getInt(actualLenBytes);

        byte[] seqNoBytes = new byte[4];
        System.arraycopy(packet, 6, seqNoBytes, 0, 4);
        seqNo = getInt(seqNoBytes);

        checkSum = computeChecksum(packet, 2, PACKET_HEADER_SIZE + length);
        byte[] receivedChecksum = new byte[2];
        System.arraycopy(chunkData, 0, receivedChecksum, 0, 2);
        isCorrupted = (checkSum == getInt(receivedChecksum) );
    }



    private static int computeChecksum(byte[] data, int start, int end) {
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

    private static byte[] getBytes(int num) {
        byte[] bytes = new byte[Integer.BYTES];
        for(int i = 0; i < Integer.BYTES; i++)
            bytes[i] = (byte) ((num & ((0xFF) << (i << 3))) >> (i << 3));
        return bytes;
    }

    private static byte[] getBytes(long num) {
        byte[] bytes = new byte[Long.BYTES];
        for(int i = 0; i < Long.BYTES; i++)
            bytes[i] = (byte) ((num & ((0xFF) << (i << 3))) >> (i << 3));
        return bytes;
    }

    private static int getInt(byte[] bytes){
        int value=0;
        for(int i=0; i<bytes.length; i++){
            value = value << Byte.SIZE;
            value += (((int)bytes[i])&0xff);
        }
        return value;
    }

    private static long getLong(byte[] bytes){
        Long value=0L;
        for(int i=0; i<bytes.length; i++){
            value = value << Byte.SIZE;
            value += (((long)bytes[i])&0xff);
        }
        return value;
    }

    public long getSeqNo() {
        return seqNo;
    }


    public static void main(String[] args) {
        byte[] data = {(byte)0b01100110, (byte)0b01100000, (byte)0b01010101,
                (byte)0b01010101, (byte)0b10001111, (byte)0b00001100};
        int checksum = computeChecksum(data, 0, 6);
        System.out.println(String.format("%h", checksum));
    }

    @Override
    public DatagramPacket createDatagramPacket() {
        byte[] packetData = new byte[length + PACKET_HEADER_SIZE];

        // first 2 bytes are reserved for checksum

        byte[] actualLenBytes = getBytes(length);
        System.arraycopy(actualLenBytes, 0, packetData, 2, 4);

        byte[] seqNoBytes = getBytes(seqNo);
        System.arraycopy(seqNoBytes, 0, packetData, 6, 4);

        System.arraycopy(chunkData, 0, packetData, PACKET_HEADER_SIZE, length);

        checkSum = computeChecksum(packetData, 2, PACKET_HEADER_SIZE + length);
        System.arraycopy(getBytes(checkSum), 0, packetData, 0, 2);

        return new DatagramPacket(packetData, packetData.length);
    }
}