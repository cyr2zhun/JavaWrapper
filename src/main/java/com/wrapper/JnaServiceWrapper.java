//package com.wrpper;
//
//import com.sun.jna.Library;
//import com.sun.jna.Native;
//import com.sun.jna.platform.win32.*;
//import com.sun.jna.platform.win32.Winsvc.*;
//import com.sun.jna.ptr.IntByReference;
//
//import java.awt.event.WindowListener;
//import java.util.Scanner;
//
//public class JnaServiceWrapper {
//
//    // JNA Windows API interfaces
//    public interface Advapi32 extends Library {
//        Advapi32 INSTANCE = Native.load("Advapi32", Advapi32.class);
//        
//        WinNT.HANDLE OpenSCManager(String lpMachineName, String lpDatabaseName, int dwDesiredAccess);
//        WinNT.HANDLE CreateService(
//            WinNT.HANDLE hSCManager, String lpServiceName, String lpDisplayName,
//            int dwDesiredAccess, int dwServiceType, int dwStartType,
//            int dwErrorControl, String lpBinaryPathName, String lpLoadOrderGroup,
//            WinDef.DWORDByReference lpdwTagId, String lpDependencies,
//            String lpServiceStartName, String lpPassword
//        );
//        boolean CloseServiceHandle(WinNT.HANDLE hSCObject);
//        boolean StartService(WinNT.HANDLE hService, int dwNumServiceArgs, String[] lpServiceArgVectors);
//        boolean SetServiceStatus(WinNT.HANDLE hServiceStatus, SERVICE_STATUS lpServiceStatus);
//    }
//
//    private static volatile boolean running = false;
//    private static WinNT.HANDLE serviceStatusHandle;
//    private static SERVICE_STATUS serviceStatus;
//    private static Thread serviceThread;
//    private static Scanner scanner;
//    private static final String SERVICE_NAME = "MyJavaService";
//
// // Correct service handler implementation for JNA 5.17.0
//    private static class ServiceHandler implements WindowListener {
//        @Override
//
//
//    // Correct service table initialization
//    public static void main(String[] args) {
//        if (args.length > 0 && args[0].equalsIgnoreCase("service")) {
//            WinSvc.INSTANCE.StartServiceCtrlDispatcher(
//                new WinSvc.SERVICE_TABLE_ENTRY[] {
//                    new WinSvc.SERVICE_TABLE_ENTRY(SERVICE_NAME, new ServiceHandler()),
//                    new WinSvc.SERVICE_TABLE_ENTRY(null, null)
//                }
//            );
//        } else {
//            runConsoleMode();
//        }
//    }
//
//    private static void serviceMain() {
//        // Register service control handler
//        serviceStatusHandle = Winsvc.INSTANCE.RegisterServiceCtrlHandler(SERVICE_NAME, new ServiceHandler());
//
//        if (serviceStatusHandle == null) {
//            System.err.println("Failed to register service control handler: " + Kernel32Util.getLastErrorMessage());
//            return;
//        }
//
//        // Initialize service status
//        serviceStatus = new SERVICE_STATUS();
//        serviceStatus.dwServiceType = Winsvc.SERVICE_WIN32_OWN_PROCESS;
//        serviceStatus.dwCurrentState = Winsvc.SERVICE_START_PENDING;
//        serviceStatus.dwControlsAccepted = Winsvc.SERVICE_ACCEPT_STOP | Winsvc.SERVICE_ACCEPT_SHUTDOWN;
//        
//        Advapi32.INSTANCE.SetServiceStatus(serviceStatusHandle, serviceStatus);
//        
//        // Start service thread
//        startService();
//    }
//
//    private static void runConsoleMode() {
//        scanner = new Scanner(System.in);
//        System.out.println("JNA Windows Service Wrapper");
//        System.out.println("Commands: install, uninstall, start, stop, run, exit");
//        
//        while (true) {
//            System.out.print("> ");
//            String command = scanner.nextLine().trim().toLowerCase();
//
//            switch (command) {
//                case "install":
//                    installService();
//                    break;
//                case "uninstall":
//                    uninstallService();
//                    break;
//                case "start":
//                    startService();
//                    break;
//                case "stop":
//                    stopService();
//                    break;
//                case "run":
//                    runServiceInConsole();
//                    break;
//                case "exit":
//                    shutdown();
//                    return;
//                default:
//                    System.out.println("Unknown command");
//            }
//        }
//    }
//
//    private static void installService() {
//        WinNT.HANDLE scm = Advapi32.INSTANCE.OpenSCManager(
//            null, null, Winsvc.SC_MANAGER_CREATE_SERVICE);
//        
//        if (scm == null) {
//            System.err.println("Failed to open service control manager: " + Kernel32Util.getLastErrorMessage());
//            return;
//        }
//
//        String javaPath = System.getProperty("java.home") + "\\bin\\java.exe";
//        String jarPath = "path\\to\\your\\YourService.jar";
//        
//        WinNT.HANDLE service = Advapi32.INSTANCE.CreateService(
//            scm,
//            SERVICE_NAME,
//            "My Java Service",
//            Winsvc.SERVICE_ALL_ACCESS,
//            Winsvc.SERVICE_WIN32_OWN_PROCESS,
//            Winsvc.SERVICE_AUTO_START,
//            Winsvc.SERVICE_ERROR_NORMAL,
//            "\"" + javaPath + "\" -jar \"" + jarPath + "\" service",
//            null,
//            null,
//            null,
//            null,
//            null
//        );
//
//        if (service == null) {
//            System.err.println("Failed to create service: " + Kernel32Util.getLastErrorMessage());
//        } else {
//            System.out.println("Service installed successfully");
//            Advapi32.INSTANCE.CloseServiceHandle(service);
//        }
//        
//        Advapi32.INSTANCE.CloseServiceHandle(scm);
//    }
//
//    private static void uninstallService() {
//        WinNT.HANDLE scm = Advapi32.INSTANCE.OpenSCManager(
//            null, null, Winsvc.SC_MANAGER_CONNECT);
//        
//        if (scm == null) {
//            System.err.println("Failed to open service control manager: " + Kernel32Util.getLastErrorMessage());
//            return;
//        }
//
//        WinNT.HANDLE service = Advapi32.INSTANCE.OpenService(
//            scm, SERVICE_NAME, Winsvc.DELETE);
//        
//        if (service == null) {
//            System.err.println("Failed to open service: " + Kernel32Util.getLastErrorMessage());
//            Advapi32.INSTANCE.CloseServiceHandle(scm);
//            return;
//        }
//
//        if (!Advapi32.INSTANCE.DeleteService(service)) {
//            System.err.println("Failed to delete service: " + Kernel32Util.getLastErrorMessage());
//        } else {
//            System.out.println("Service uninstalled successfully");
//        }
//        
//        Advapi32.INSTANCE.CloseServiceHandle(service);
//        Advapi32.INSTANCE.CloseServiceHandle(scm);
//    }
//
//    private static void startService() {
//        if (running) {
//            System.out.println("Service already running");
//            return;
//        }
//        
//        running = true;
//        serviceThread = new Thread(() -> {
//            System.out.println("Service running...");
//            
//            // Update status to RUNNING
//            if (serviceStatus != null) {
//                serviceStatus.dwCurrentState = Winsvc.SERVICE_RUNNING;
//                Advapi32.INSTANCE.SetServiceStatus(serviceStatusHandle, serviceStatus);
//            }
//            
//            // Main service loop
//            while (running) {
//                try {
//                    // Do service work here
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    running = false;
//                }
//            }
//            
//            // Service stopping
//            if (serviceStatus != null) {
//                serviceStatus.dwCurrentState = Winsvc.SERVICE_STOP_PENDING;
//                Advapi32.INSTANCE.SetServiceStatus(serviceStatusHandle, serviceStatus);
//            }
//            
//            // Final status update
//            if (serviceStatus != null) {
//                serviceStatus.dwCurrentState = Winsvc.SERVICE_STOPPED;
//                Advapi32.INSTANCE.SetServiceStatus(serviceStatusHandle, serviceStatus);
//            }
//        });
//        
//        serviceThread.start();
//    }
//
//    private static void stopService() {
//        if (!running) {
//            System.out.println("Service not running");
//            return;
//        }
//        
//        System.out.println("Stopping service...");
//        running = false;
//        if (serviceThread != null) {
//            serviceThread.interrupt();
//        }
//    }
//
//    private static void runServiceInConsole() {
//        System.out.println("Running service in console mode (Press Enter to stop)");
//        serviceMain(); // Run as if it were a service
//        
//        try {
//            System.in.read();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        
//        stopService();
//        System.out.println("Service stopped");
//    }
//
//    private static void shutdown() {
//        if (running) {
//            stopService();
//        }
//        if (scanner != null) {
//            scanner.close();
//        }
//        System.out.println("Wrapper shutdown complete");
//    }
//}


