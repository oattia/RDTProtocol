package com.rdt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ServerConfig {
    private int port;
    private int maxN;
    private long rngSeed;
    private float plp;
    private String strategy;

    private ServerConfig() {

    }

    public static ServerConfig parseConfigFile(String fileName) throws IOException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(fileName));
        } catch (IOException e) {
            throw e;
        }

        ServerConfig sc = new ServerConfig();
        sc.port = Integer.parseInt(in.readLine());
        sc.maxN = Integer.parseInt(in.readLine());
        sc.rngSeed= Long.parseLong(in.readLine());
        sc.plp = Float.parseFloat(in.readLine());
        sc.strategy = in.readLine();
        sc.strategy = sc.strategy == null ? "StopAndWait" : sc.strategy;
        in.close();
        return sc;
    }

    public int getPort() {
        return port;
    }

    public int getMaxN() {
        return maxN;
    }

    public long getRngSeed() {
        return rngSeed;
    }

    public float getPlp() {
        return plp;
    }

    public String getStrategy() {
        return strategy;
    }
}
