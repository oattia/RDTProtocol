package com.rdt;

public abstract class TransmissionStrategy {

    protected int numOfPackets;
    protected int initSeqNo;
    protected int windowSize;
    protected long nextPacketToSend;

    protected int windowStart;
    protected int windowEnd;

    public static final String STOP_AND_WAIT = "StopAndWait";
    public static final String GO_BACK_N = "GoBackN";
    public static final String SELECTIVE_REPEAT = "SelectiveRepeat";


    public TransmissionStrategy(int numOfPackets, int initSeqNo, int initWindowSize){
        this.numOfPackets = numOfPackets;
        this.initSeqNo = initSeqNo;
        this.windowSize = initWindowSize;

        nextPacketToSend = 1;
        windowStart = 1;
        windowEnd = windowStart+windowSize;
    }

    abstract boolean isDone();

    abstract void sent(long seqNo);

    abstract void acknowledged(long seqNo);

    abstract void timedout(long seqNo);

    abstract long getNextSeqNo();

    public int[] getWindow(){
        int [] w = {windowStart, windowEnd};
        return w;
    }
}