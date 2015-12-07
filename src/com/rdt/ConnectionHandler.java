package com.rdt;

import com.rdt.utils.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectionHandler implements Runnable, Subscriber {

    private AtomicBoolean acceptingTimeouts;
    private BlockingQueue<Event> mailbox;
    private TransmissionStrategy strategy;
    private DatagramSocket socket;
    private SocketListener socketListener;
    private Thread socketListenerThread;
    private FileInputStream fileStream;
    private Map<Long, TimeoutTimerTask> timeoutMap;
    private Random rng;
    private Set<Long> timedoutNotAcked;
    private float plp = 0.05f;         // packet loss probability: from 0 to 100
    private float pep = 0.05f;         // packet error probability: from 0 to 100
    private String strategyName;
    private String fileName;
    private int windowSize;

    private double estimatedRtt = 1000.0d;
    private double devRtt = 0.0;
    private long timeoutInterval = 1000L; // In milliseconds

    private int destPort;
    private InetAddress destIp;

    private static final Timer TIMER = new Timer(true);
    private static final long NICENESS = 50L; // milliseconds to sleep every iteration
    private static final int CHUNK_SIZE = 64000;
    private static final float ALPHA = 0.125f;
    private static final float BETA = 0.25f;
    private static final long MAX_PKT_TIMEOUT = 60_000L;

    public ConnectionHandler(String strategyName, RequestPacket request, float plp, float pep, long seed, int windowSize) {

        this.strategyName = strategyName;
        this.fileName = request.getFileName();
        this.destPort = request.getPort();
        this.destIp = request.getIp();

        this.plp = plp;
        this.pep = pep;
        this.rng = new Random(seed);
        this.windowSize = windowSize;
        this.timedoutNotAcked = new HashSet<>();
        acceptingTimeouts = new AtomicBoolean(true);
    }

    private boolean init() {
        File file;
        try {
            file = new File(fileName);
            fileStream = new FileInputStream(file);
        }catch(FileNotFoundException e) {
            sendNotFoundPacket();
            return false;
        }

        int numOfChunx = (int) Math.ceil(file.length() / CHUNK_SIZE);
        int initialSeqNo = rng.nextInt(1000);

        if (strategyName == null) {
            throw new IllegalArgumentException();
        } else if(strategyName.equalsIgnoreCase(TransmissionStrategy.STOP_AND_WAIT)){
            strategy = new StopAndWaitStrategy(numOfChunx, initialSeqNo);
        } else if (strategyName.equalsIgnoreCase(TransmissionStrategy.SELECTIVE_REPEAT)){
            strategy = new SelectiveRepeatStrategy(numOfChunx, initialSeqNo, windowSize);
        } else if (strategyName.equalsIgnoreCase(TransmissionStrategy.GO_BACK_N)) {
            strategy = new GoBackNStrategy(numOfChunx, initialSeqNo, windowSize);
        } else {
            throw new IllegalArgumentException();
        }

        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            // TODO
        }

        mailbox = new LinkedBlockingQueue<>();
        timeoutMap = new HashMap<>();

        socketListener = new SocketListener(socket);
        socketListener.subscribe(this);

        socketListenerThread = new Thread(socketListener);
        socketListenerThread.start();
        return true;
    }

    @Override
    public void run() {
        if(!init())
            return;

        while (!strategy.isDone() && timeoutInterval <= MAX_PKT_TIMEOUT) {
            long seqNo = strategy.getNextSeqNo();

            if(seqNo != -1L) {
                DataPacket pkt;
                if(!timeoutMap.containsKey(seqNo)) {
                    pkt = makeDataPacket(seqNo);
                } else {
                    TimeoutTimerTask ttt = timeoutMap.get(seqNo);
                    if(ttt.scheduledExecutionTime() > System.currentTimeMillis()){
                        if(timedoutNotAcked.contains(seqNo)){

                        } else {

                        }
                    } else {

                    }
                    pkt = (DataPacket) timeoutMap.get(seqNo).getPkt();
                }
                sendDataPacket(pkt);
                setTimer(pkt, seqNo);
            }

            if(!mailbox.isEmpty()) {
                consumeMailbox();
            }

            try {
                Thread.sleep(NICENESS);
            } catch (InterruptedException e){
                // TODO
            }
            //
        }
        clean();
    }

    private void clean() {
        socket.close();
        for(Map.Entry<Long, TimeoutTimerTask> e : timeoutMap.entrySet()){
            e.getValue().cancel();
        }
        timeoutMap.clear();
        TIMER.purge();
    }

    private void sendNotFoundPacket() {

    }

    private DataPacket makeDataPacket(long seqNo) {
        byte[] data = new byte[CHUNK_SIZE];
        int actualLen;
        try {
            actualLen = fileStream.read(data);
        } catch(IOException e) {
            return null;
        }
        return new DataPacket(data, actualLen, seqNo, destPort, destIp);
    }

    private void sendDataPacket(DataPacket pkt) {
        try {
            if(rng.nextFloat() < pep) {
                byte[] data = pkt.getChunkData();
                int bitWithError = rng.nextInt(8 * data.length);
                data[(bitWithError / 8)] ^= (1 << (bitWithError % 8));
                pkt.setChunkData(data);
            }

            if(rng.nextFloat() >= plp)
                socket.send(pkt.createDatagramPacket());

            strategy.sent(pkt.getSeqNo());
        } catch (IOException e){
            //TODO
        }
    }

    private void consumeMailbox() {
        boolean firstTimeout = true;
        while (!mailbox.isEmpty()) {
            Event e = mailbox.poll();
            if(e instanceof TimeoutEvent) {
                TimeoutEvent tt = (TimeoutEvent)e;
                if(firstTimeout) {
                    firstTimeout = false;
                    handleTimeoutEvent(tt);
                } else {
                   if(timeoutMap.get(tt.getSeqNo()).scheduledExecutionTime() > System.currentTimeMillis()){
                       // Do nothing ...
                       // This is rescheduled
                   } else {
                        handleTimeoutEvent(tt);
                   }
                }
            } else if(e instanceof AckEvent) {
                handleAckEvent((AckEvent) e);
            }
        }
    }

    private void handleAckEvent(AckEvent e) {
        long timeNow = System.currentTimeMillis();
        long seqNo = e.getAckNo();
        TimeoutTimerTask ttt = timeoutMap.remove(seqNo);
        if(ttt != null) {
            ttt.cancel();
            if(!timedoutNotAcked.contains(seqNo)) {
                double sampleRtt = (double) (timeNow - ttt.getTimestamp());
                estimatedRtt = (1.0f - ALPHA) * estimatedRtt + ALPHA * sampleRtt;
                devRtt = (1.0f - BETA) * devRtt + BETA * Math.abs(sampleRtt - estimatedRtt);
                timeoutInterval = (long) Math.ceil(estimatedRtt + 4.0f * devRtt);
            } else {
                timedoutNotAcked.remove(seqNo);
            }
        }
        strategy.acknowledged(seqNo);
    }

    private void handleTimeoutEvent(TimeoutEvent e) {
        acceptingTimeouts.compareAndSet(true, false);

        long seqNo = e.getSeqNo();
        strategy.timedout(seqNo);
        timeoutMap.remove(seqNo);
        timedoutNotAcked.add(seqNo);

        Map<Long, TimeoutTimerTask> newMap = new HashMap<>();

        for(Map.Entry<Long, TimeoutTimerTask> entry: timeoutMap.entrySet()) {
            long seqNoE = entry.getKey();
            TimeoutTimerTask tttE = entry.getValue();
            tttE.cancel();
            long sysMillis = System.currentTimeMillis();
            long newDelay = tttE.getTimestamp() + 2L * tttE.getDelay() - sysMillis;
            TimeoutTimerTask newTttE = new TimeoutTimerTask(seqNoE, sysMillis, newDelay, tttE.getPkt());
            TIMER.schedule(newTttE, newDelay);
            newMap.put(seqNoE, newTttE);
        }

        timeoutMap.clear();
        timeoutMap.putAll(newMap); // Maybe timeoutMap = newMap instead?
        TIMER.purge();

        timeoutInterval *= 2L; //Exponential, must guard against!

        acceptingTimeouts.getAndSet(true);
    }

    private void setTimer(Packet pkt, long seqNo) {
        TimeoutTimerTask ttt = new TimeoutTimerTask(seqNo, System.currentTimeMillis(), timeoutInterval, pkt);
        ttt.subscribe(this);
        timeoutMap.put(seqNo, ttt);
        TIMER.schedule(ttt, timeoutInterval);
    }

    @Override
    public void update(Event e) {
        if(e instanceof AckEvent) {
            mailbox.offer(e);
        } else if (e instanceof TimeoutEvent) {
            if(acceptingTimeouts.get())
                mailbox.offer(e);
        } else {
            // Do nothing ...
        }
    }
}
