package com.rdt;

public class GoBackNStrategy extends TransmissionStrategy {
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

    @Override
    int[] getWindow() {
        return new int[0];
    }
}
