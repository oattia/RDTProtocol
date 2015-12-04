package com.rdt;

import com.rdt.utils.Subscriber;

import java.util.HashSet;
import java.util.Set;

public abstract class TransmissionStrategy {

    int numOfPackets;
    int initSeqNo;
    int windowSize;
    long nextPacketToSend;

    int windowStart;
    int windowEnd;

    public TransmissionStrategy(int numOfPackets, int initSeqNo, int initWindowSize){
        this.numOfPackets = numOfPackets;
        this.initSeqNo = initSeqNo;
        this.windowSize = initWindowSize;

        nextPacketToSend = 1;
        windowStart = 1;
        windowEnd = windowStart+windowSize;
    }

    private Set<Subscriber> subscribers = new HashSet<>();

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