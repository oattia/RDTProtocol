package com.rdt;

import com.rdt.utils.Event;
import com.rdt.utils.Publisher;
import com.rdt.utils.Subscriber;

import java.net.DatagramSocket;
import java.util.HashSet;
import java.util.Set;

public class SocketListener implements Runnable, Publisher {

    private DatagramSocket socket;

    private Set<Subscriber> subscribers = new HashSet<>();

    public SocketListener(DatagramSocket socket){
        this.socket = socket;
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

    }
}
