package com.rdt;

import com.rdt.utils.Event;
import com.rdt.utils.Publisher;
import com.rdt.utils.Subscriber;

import java.util.HashSet;
import java.util.Set;

public abstract class TransmissionStrategy implements Publisher {

    private Set<Subscriber> subscribers = new HashSet<>();

    abstract boolean isDone();

    abstract void sent(long seqNo);

    abstract void acknowledged(long seqNo);

    abstract void timedout(long seqNo);

    abstract long getNextSeqNo();

    abstract int[] getWindow();

    @Override
    public void publish(Event e) {
        for(Subscriber s : subscribers) {
            s.update(e);
        }
    }

    @Override
    public void subscribe(Subscriber s) {
        subscribers.add(s);
    }

    @Override
    public void unsubscribe(Subscriber s) {
        subscribers.remove(s);
    }
}