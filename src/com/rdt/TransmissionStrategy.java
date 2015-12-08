package com.rdt;

public abstract class TransmissionStrategy {

    // To check if done ...
    protected int numOfPackets;
    protected long initSeqNo;

    // Running variables
    protected int windowSize;
    protected long base; // first not acked.
    protected long nextSeqNum;

    private int ssthreshold = 20;
    private double cwnd = 0.0;      // used to save info about current window size

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
        this.cwnd = (double)initWindowSize;

        this.nextSeqNum = initSeqNo;
        this.base = initSeqNo;
    }

    protected void updateWinSize_timeout(){
        this.ssthreshold /= 2;
        this.windowSize /= 2;       // if packet is lost: window size is halved
        System.out.println("*********------------ "+this.windowSize);
    }

    protected void updateWinSize_ackRecv(){
        int temp = this.windowSize;

        if( this.windowSize >= ssthreshold ) {
            cwnd += 1.0/this.windowSize;
            System.out.println("-------------- "+cwnd);
            System.out.println("-------------- "+(int)Math.floor(cwnd));
            this.windowSize = (int)Math.floor(cwnd);
        } else {
            this.windowSize ++;
            this.cwnd ++;
        }

        if(this.windowSize!=temp)
            System.out.println("********************* "+this.windowSize);
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

    public int getNumOfPackets() {
        return numOfPackets;
    }

    public long getInitSeqNo() {
        return initSeqNo;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public long getBase() {
        return base;
    }

    public long getNextSeqNum() {
        return nextSeqNum;
    }
}