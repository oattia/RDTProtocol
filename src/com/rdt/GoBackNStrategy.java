package com.rdt;

public class GoBackNStrategy extends TransmissionStrategy {
    public GoBackNStrategy(int numOfPackets, int initSeqNo, int initWindowSize) {
        super(numOfPackets, initSeqNo, initWindowSize);
    }

    @Override
    boolean isDone() {
        return false;
    }

    @Override
    void sent(long seqNo) {

    }

    @Override
    void acknowledged(long seqNo) {

    }

    @Override
    void timedout(long seqNo) {

    }

    @Override
    long getNextSeqNo() {
        return 0;
    }

}
