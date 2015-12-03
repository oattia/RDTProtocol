package com.rdt;

import com.rdt.utils.*;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class ConnectionHandler implements Runnable, Subscriber {

    private BlockingQueue<Event> mailbox;
    private TransmissionStrategy strategy;
    private DatagramSocket socket;
    private SocketListener socketListener;
    private File file;
    private Map<Long, TimerTask> timeoutMap;

    private static final Timer timer = new Timer(true);

    private static final long NICENESS = 50L; // milliseconds to sleep every iteration

    public ConnectionHandler(String strategyName, String fileName){

        if (strategyName == null){
            throw new IllegalArgumentException();
        } else if(strategyName.equalsIgnoreCase("StopAndWait")){
            strategy = new StopAndWaitStrategy();
        }else if (strategyName.equalsIgnoreCase("SelectiveRepeat")){
            strategy = new SelectiveRepeatStrategy();
        } else if (strategyName.equalsIgnoreCase("GoBackN")) {
            strategy = new GoBackNStrategy();
        } else {
            throw new IllegalArgumentException();
        }
        strategy.subscribe(this);

        try {
            socket = new DatagramSocket();
        } catch (SocketException e){
            // TODO
        }
        socketListener = new SocketListener(socket);
        socketListener.subscribe(this);

        mailbox = new LinkedBlockingDeque<>();
        timeoutMap = new HashMap<>();
    }

    @Override
    public void run() {
        while(!strategy.isDone()) {
            try {
                Thread.sleep(NICENESS);
            } catch (InterruptedException e){
                // TODO
            }

            long seqNo = strategy.getNextSeqNo();

            if(seqNo != -1L) {
                sendDataPacket(makeDataPacket(seqNo));
                setTimer(seqNo);
            }

            if(!mailbox.isEmpty()) {
                consumeMailbox();
            }
        }
    }

    private void consumeMailbox() {
        while (!mailbox.isEmpty()) {
            Event e = mailbox.poll();
            if(e instanceof TimeoutEvent){
                handleTimeoutEvent((TimeoutEvent) e);
            } else if(e instanceof AckEvent) {
                handleAckEvent((AckEvent) e);
            }
        }
    }

    private DataPacket makeDataPacket(long seqNo){
        return null;
    }

    // TODO: simulate packet loss.
    private void sendDataPacket(DataPacket pkt) {
        try {
            socket.send(pkt.getDatagramPacket());
            strategy.sent(pkt.getSeqNo());
        } catch (IOException e){
            //TODO
        }
    }

    private void handleAckEvent(AckEvent e) {
        long seqNo = e.getSeqNo();
        TimerTask ttt = timeoutMap.remove(seqNo);
        if(ttt != null) ttt.cancel();
        strategy.acknowledged(seqNo);
    }

    private void handleTimeoutEvent(TimeoutEvent e) {
        long seqNo = e.getSeqNo();
        timeoutMap.remove(seqNo);
        strategy.timedout(seqNo);
    }

    private void setTimer(long seqNo) {
        TimeoutTimerTask ttt = new TimeoutTimerTask(seqNo);
        ttt.subscribe(this);
        timeoutMap.put(seqNo, ttt);
        timer.schedule(ttt, 10L);
    }

    @Override
    public void update(Event e) {
        if(e instanceof AckEvent || e instanceof TimeoutEvent) {
            mailbox.offer(e);
        } else {
            // TODO
            return;
        }
    }
}
