package com.rdt;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Server {

    private static class Worker {
        private Thread thread;
        private long timestamp;

        public Worker(Thread t, long timestamp){
            this.thread = t;
            this.timestamp = timestamp;
        }

        public Thread getThread() {
            return thread;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    private DatagramSocket welcomingSocket;
    private int maxN;
    private long rngSeed;
    private float plp;
    private float pep = 0.05f;
    private String strategy;
    private List<Worker> workers;

    private static final long WORKER_MAX_TIME = 10_000_000L; // In milliseconds (~3 hours)
    private static final long MAX_PAR_WORKERS = 1000;
    private static final int EXPECTED_REQ_SIZE = 2048;

    public Server (ServerConfig serverConfig){

    }

    public Server(int port, int maxN, long rngSeed, float plp, String strategy) throws SocketException {
        try {
            welcomingSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw e;
        }

        this.maxN = maxN;
        this.rngSeed = rngSeed;
        this.plp = plp;
        this.strategy = strategy;
        this.workers = new LinkedList<>();
    }

    public void run() {
        while(!welcomingSocket.isClosed()) {
            while (!canCreateNewWorker()) {
               try {
                   Thread.sleep(1000);
               } catch (InterruptedException e) {
                   continue;
               }
            }
            DatagramPacket pkt = new DatagramPacket(new byte[EXPECTED_REQ_SIZE], EXPECTED_REQ_SIZE);

            try {
                welcomingSocket.receive(pkt);
            } catch (IOException e) {
                continue;
            }

            String fileName = "";
            Thread connectionHandler = new Thread(
                    new ConnectionHandler(strategy, fileName,
                    plp, pep,  rngSeed, maxN)
            );
            connectionHandler.start();
            workers.add(new Worker(connectionHandler, System.currentTimeMillis()));
        }
    }

    private boolean canCreateNewWorker() {
        Iterator<Worker> it = workers.iterator();
        while (it.hasNext()) {
            Worker worker = it.next();
            if(!worker.getThread().isAlive() ||
                    (System.currentTimeMillis() - worker.getTimestamp() > WORKER_MAX_TIME)){
                worker.getThread().stop();
                it.remove();
            }
        }
        return workers.size() < MAX_PAR_WORKERS;
    }
}