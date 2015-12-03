package com.rdt.utils;

public class AckEvent implements Event {
    private long seqNo;

    public long getSeqNo() {
        return seqNo;
    }
}
