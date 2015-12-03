package com.rdt.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;

public class TimeoutTimerTask extends TimerTask implements Publisher {

    private Set<Subscriber> subscribers = new HashSet<>();
    private long seqNo;

    public TimeoutTimerTask(long seqNo){
        this.seqNo = seqNo;
    }

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

    @Override
    public void run() {
        publish(new TimeoutEvent(seqNo));
    }
}
