package com.rdt;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        String configFileName = args[0];
        ServerConfig serverConfig = null;
        try {
            serverConfig = ServerConfig.parseConfigFile(configFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Server server = new Server(serverConfig);
        server.run();
    }
}
