package com.androidsrc.snake_game;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;

public class Clienthandler {
    static int numberofsockets = 1;
    MainActivity activity;
    boolean ServerOn;
    String dstAddress;
    int dstPort = 8080;
    static int clientthreadcount = 0 ;
    static Socket clientsendersocket;
    static HashMap<String, Socket> sockethashmap = new HashMap<String, Socket>();
    static HashMap<Socket,Boolean> activesocketsinfo = new HashMap<Socket, Boolean>();
    Object testmessage = "Hello from Client";


    public Clienthandler(MainActivity activity, String ip) {
        this.activity = activity;
        dstAddress = ip;
        Thread clienthandler = new Thread(new clienthandlerthread());
        clienthandler.start();
    }

    public int getPort() {
        return dstPort;
    }

    public static Socket getsocket(String key) {
        return sockethashmap.get(key);
    }

    public static void onDestroy() {
        if (clientsendersocket != null) {
            try {
                clientsendersocket.close();
                //if the socket is closed reset all the parameters to default
                clientthreadcount = 0;
                numberofsockets = 0;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class clienthandlerthread extends Thread {
        @Override
        public void run() {
            //primary check to see if the socket is already open or not
            while (numberofsockets <= 1) {
                try {
                    clientsendersocket = new Socket(dstAddress, dstPort);
                    //Connection is ok. Update the sockets info hashmap and limiting parameters
                    numberofsockets++; //don't have more than one server socket open for the client in the snake_game
                    sockethashmap.put("Server", clientsendersocket);
                    ServerOn = true;
                    activesocketsinfo.put(clientsendersocket, ServerOn);
                    while (ServerOn) {
                        if (clientthreadcount < 2) {
                            ClientsenderThread cliThread = new ClientsenderThread(clientsendersocket, activity, testmessage);
                            Clientlistenerthread cliThread2 = new Clientlistenerthread(clientsendersocket,activity);
                            //Number of threads
                            clientthreadcount = 2;
                            final int nbThreads = Thread.getAllStackTraces().keySet().size();
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    activity.msg.append("\n Client thread created for:" + clientsendersocket.getInetAddress() + " Number of threads now : " + nbThreads);
                                }
                            });
                            cliThread.start();
                            cliThread2.start();
                        }
                    }
                } catch (IOException e) {
                    // Exception that socket has a problem. Update the acitvesockets hashmap
                    ServerOn = false;
                    activesocketsinfo.put(clientsendersocket, ServerOn);
                    e.printStackTrace();
                }
            }
        }
    }

    public String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress
                            .nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "Serverhandler running at : "
                                + inetAddress.getHostAddress();
                    }
                }
            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }

}