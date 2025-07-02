package com.wrapper;

public class MyRuntime {
    public static volatile boolean running = true;
    
    public static void main(String[] args) throws InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;
            System.out.println("Shutdown signal received!");
        }));
        
        while (running) {
            // Your service logic here
            System.out.println("MyRuntime is Running...");
            Thread.sleep(1000);
        }
    }
}
