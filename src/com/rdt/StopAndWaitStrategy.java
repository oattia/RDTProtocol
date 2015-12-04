package com.rdt;

public class StopAndWaitStrategy extends TransmissionStrategy {

    public StopAndWaitStrategy(int numOfPackets, int initSeqNo) {
        super(numOfPackets, initSeqNo, 1);
    }

    @Override
    public boolean isDone() {
        return (nextPacketToSend==numOfPackets+1);      // next packet to send is 1 based
    }

    @Override
    void sent(long seqNo) {
        nextPacketToSend++;
    }

    @Override
    public void acknowledged(long seqNo) {
        windowStart++;
        windowEnd++;
    }

    @Override
    void timedout(long seqNo) {
        nextPacketToSend--;
    }

    @Override
    public long getNextSeqNo() {
        if(nextPacketToSend >= windowStart && nextPacketToSend < windowEnd){
            return nextPacketToSend;
        }
        return -1L;
    }

}