package com.rdt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        // String configFileName = args[0];
        String configFileName = "./Server.in";
        try {
            ServerConfig serverConfig = ServerConfig.parseConfigFile(configFileName);
            Server server = new Server(serverConfig);
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
         }
    }
}
