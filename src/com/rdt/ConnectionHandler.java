package com.rdt;

import com.rdt.utils.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionHandler implements Runnable, Subscriber {

    private BlockingQueue<Event> mailbox;
    private TransmissionStrategy strategy;
    private DatagramSocket socket;
    private SocketListener socketListener;
    private Thread socketListenerThread;
    private FileInputStream fileStream;
    private Map<Long, TimerTask> timeoutMap;
    private Random randomGenerator;

    private static final Timer timer = new Timer(true);

    private static final long NICENESS = 50L; // milliseconds to sleep every iteration
    private static final int CHUNK_SIZE = 64000;
    private float plp = 0.05f;         // packet loss probability: from 0 to 100
    private float pep = 0.05f;         // packet error probability: from 0 to 100

    private String strategyName;
    private String fileName;
    private int windowSize;

    public ConnectionHandler(String strategyName, String fileName,
                             float plp, float pep, long seed, int windowSize){

        this.strategyName = strategyName;
        this.fileName = fileName;

        this.plp = plp;
        this.pep = pep;
        this.randomGenerator = new Random(seed);
        this.windowSize = windowSize;
    }

    public boolean init(){
        File file;
        try {
            file = new File(fileName);
            fileStream = new FileInputStream(file);
        }catch(FileNotFoundException e) {
            sendNotFoundPacket();
            return false;
        }

        int numOfChunx = (int)Math.ceil(file.length() / CHUNK_SIZE);
        int initialSeqNo = randomGenerator.nextInt(1000);

        if (strategyName == null) {
            throw new IllegalArgumentException();
        } else if(strategyName.equalsIgnoreCase("StopAndWait")){
            strategy = new StopAndWaitStrategy(numOfChunx, initialSeqNo);
        }else if (strategyName.equalsIgnoreCase("SelectiveRepeat")){
            strategy = new SelectiveRepeatStrategy(numOfChunx, initialSeqNo, windowSize);
        } else if (strategyName.equalsIgnoreCase("GoBackN")) {
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
        if( !init() )
            return;

        while(!strategy.isDone()) {
            long seqNo = strategy.getNextSeqNo();

            if(seqNo != -1L) {
                sendDataPacket(makeDataPacket(seqNo));
                setTimer(seqNo);
            }

            if(!mailbox.isEmpty()) {
                consumeMailbox();
            }

            try {
                Thread.sleep(NICENESS);
            } catch (InterruptedException e){
                // TODO
            }
        }
        socket.close();
    }

    private void sendNotFoundPacket() {

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

    private DataPacket makeDataPacket(long seqNo) {
        byte[] data = new byte[CHUNK_SIZE];
        int actualLen;
        try {
            actualLen = fileStream.read(data);
        }catch(IOException e){
            return null;
        }

        DataPacket packet = new DataPacket(data, actualLen, seqNo);
        return packet;
    }

    // TODO: simulate packet loss.
    private void sendDataPacket(DataPacket pkt) {
        try {
            if( randomGenerator.nextFloat() < plp )
                return;
            if( randomGenerator.nextFloat() < pep ) {
                byte[] data = pkt.getChunkData();
                int bitWithError = randomGenerator.nextInt(8*data.length);
                data[(int)(bitWithError/8)] ^= (1<<(bitWithError%8));
                pkt.setChunkData(data);
            }

            socket.send(pkt.createDatagramPacket());
            strategy.sent(pkt.getSeqNo());
        } catch (IOException e){
            //TODO
        }
    }

    private void handleAckEvent(AckEvent e) {
        long seqNo = e.getAckNo();
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
        //TODO: calculate dynamic timeout
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
