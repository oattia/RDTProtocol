package com.rdt;

public abstract class TransmissionStrategy {

    // To check if done ...
    protected int numOfPackets;
    protected long initSeqNo;

    // Running variables
    protected int windowSize;
    protected long base; // first not acked.
    protected long nextSeqNum;

    // Invariants:
    // first pkt in window = base
    // last  pkt in window = base + windowSize - 1

    public static final String STOP_AND_WAIT = "StopAndWait";
    public static final String GO_BACK_N = "GoBackN";
    public static final String SELECTIVE_REPEAT = "SelectiveRepeat";


    public TransmissionStrategy(int numOfPackets, long initSeqNo, int initWindowSize) {
        this.numOfPackets = numOfPackets;
        this.initSeqNo = initSeqNo;
        this.windowSize = initWindowSize;

        this.nextSeqNum = initSeqNo;
        this.base = initSeqNo;
    }

    public abstract boolean isDone();

    public abstract void sent(long seqNo);

    public abstract void acknowledged(long seqNo);

    public abstract void timedout(long seqNo);

    public abstract long getNextSeqNo();

    public long[] getWindow() {
        long[] w = { base, base + windowSize };
        return w;
    }
}