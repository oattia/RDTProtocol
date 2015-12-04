package com.rdt;

public class SelectiveRepeatStrategy extends TransmissionStrategy {

    public SelectiveRepeatStrategy(int numOfPackets, int initSeqNo, int initWindowSize) {
        super(numOfPackets, initSeqNo, initWindowSize);
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    void sent(long seqNo) {

    }

    @Override
    public void acknowledged(long seqNo) {

    }

    @Override
    void timedout(long seqNo) {

    }

    @Override
    public long getNextSeqNo() {
        return 0;
    }

}
