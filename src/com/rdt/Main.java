package com.rdt;

public class Main {

    public static void main(String[] args) {
        Server.ServerConfig serverConfig = Server.ServerConfig.parseConfigFile(args[0]);
        Server server = new Server(serverConfig);
        server.run();
    }
}
