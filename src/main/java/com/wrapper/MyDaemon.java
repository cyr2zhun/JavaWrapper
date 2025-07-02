package com.wrapper;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;

public class MyDaemon implements Daemon {
    private static volatile boolean running = false;
    
    public static void main(String[] args) throws Exception {
        MyDaemon daemon = new MyDaemon();
        daemon.init(null);
        daemon.start();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                daemon.stop();
                daemon.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        
        // Keep the main thread alive
        while (running) {
            Thread.sleep(1000);
        }
    }

    @Override
    public void init(DaemonContext context) throws Exception {
        System.out.println("Initializing daemon...");
    }

    @Override
    public void start() throws Exception {
        System.out.println("Starting daemon...");
        running = true;
        // Your service logic here
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Stopping daemon...");
        running = false;
    }

    @Override
    public void destroy() {
        System.out.println("Cleaning up resources...");
    }
}