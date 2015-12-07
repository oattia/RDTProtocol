package com.rdt;

import java.util.HashSet;
import java.util.Set;

public class SelectiveRepeatStrategy extends TransmissionStrategy {

    private Set<Long> lostSeqs;
    private Set<Long> notAcked;     // lostSeqs is subset of notAcked

    public SelectiveRepeatStrategy(int numOfPackets, int initSeqNo, int initWindowSize) {
        super(numOfPackets, initSeqNo, initWindowSize);
        lostSeqs = new HashSet<>();
        notAcked = new HashSet<>();
    }

    @Override
    public boolean isDone() {
        return (base == (numOfPackets + initSeqNo)) && notAcked.isEmpty();
    }

    @Override
    public void sent(long seqNo) {
        if(seqNo == nextSeqNum)
            nextSeqNum++;
        notAcked.add(seqNo);
    }

    @Override
    public void acknowledged(long seqNo) {
        lostSeqs.remove(seqNo);
        notAcked.remove(seqNo);
        if (seqNo == base) {
            while(!notAcked.contains(base))
                base++;
        } else {
            // Ack out of order ... don't slide the window
        }
        // Congestion logic.
    }

    @Override
    public void timedout(long seqNo) {
        lostSeqs.add(seqNo);     // assuming it can't be acknowledged before
    }

    @Override
    public long getNextSeqNo() {
        if(!lostSeqs.isEmpty()) {
            long seqNo = lostSeqs.iterator().next();
            lostSeqs.remove(seqNo);
            return seqNo;
        } else if(nextSeqNum >= base && nextSeqNum < base + windowSize){
            return nextSeqNum;
        } else {
            return -1;
        }
    }

}
