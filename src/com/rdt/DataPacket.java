package com.rdt;

import java.net.DatagramPacket;

public class DataPacket extends Packet {

    private long seqNo; // Should be 32 bit only
    private byte[] data;

    private static final int PACKET_HEADER_SIZE = 50;

    public DataPacket(byte[] chunkData, int actualLen, long seqNo){
        data = new byte[actualLen + PACKET_HEADER_SIZE];

        // first 2 bytes are reserved for checksum

        byte[] actualLenBytes = getBytes(actualLen);
        System.arraycopy(actualLenBytes, 0, data, 2, 4);

        byte[] seqNoBytes = getBytes(seqNo);
        System.arraycopy(seqNoBytes, 0, data, 6, 4);

        System.arraycopy(chunkData, 0, data, PACKET_HEADER_SIZE, actualLen);

        length = actualLen;
        checkSum = computeChecksum(data, 2, PACKET_HEADER_SIZE + actualLen);
        System.arraycopy(getBytes(checkSum), 0, data, 0, 2);

        datagram = new DatagramPacket(data, actualLen + PACKET_HEADER_SIZE);

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

    public long getSeqNo() {
        return seqNo;
    }

    public static void main(String[] args) {
        byte[] data = {(byte)0b01100110, (byte)0b01100000, (byte)0b01010101,
                (byte)0b01010101, (byte)0b10001111, (byte)0b00001100};
        int checksum = computeChecksum(data, 0, 6);
        System.out.println(String.format("%h", checksum));
    }
}