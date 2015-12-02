package com.rdt;

import java.io.File;
import java.net.DatagramSocket;

public class ConnectionHandler implements Runnable {

    private TransmissionStrategy strategy;
    private DatagramSocket socket;
    private File file;

    @Override
    public void run() {
        while(true){

        }
    }
}
