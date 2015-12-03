package com.rdt;

public class SelectiveRepeatStrategy extends TransmissionStrategy {

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

    @Override
    public int[] getWindow() {
        return new int[0];
    }
}
