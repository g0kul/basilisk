package com.androidsrc.snake_game.communication;


import android.content.Context;

import com.androidsrc.snake_game.MainActivity;
import com.androidsrc.snake_game.R;
import com.androidsrc.snake_game.game.HostFragment;
import com.androidsrc.snake_game.game.MainFragment;
import com.androidsrc.snake_game.snakegame.SnakeCommBuffer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;


public class ServerConnThread {
	static MainActivity activity;
	ServerSocket myserverSocket;
	static final int socketServerPORT = 8080;
	boolean ServerOn = false;
	public boolean allplayersjoined = false;
	//public String username = "player1";
	public static HashMap<Socket , Integer> socketHashMapID = new HashMap();
	public static HashMap<Socket , String> socketHashMapUName = new HashMap();
	static Object testmessage;
	public static Context context;


	public ServerConnThread(Context mycontext) {
		Thread socketServerThread = new Thread(new SocketServerThread());
		socketServerThread.start();
		context = mycontext;
	}

	public int getPort() {
		return socketServerPORT;
	}

	//TODO: Ondestroy kill all the sockets connected to the server
	public static void onDestroy() {
		Iterator<Socket> socketIterator = ServerConnThread.socketHashMapID.keySet().iterator();
		Socket socket;
		while (socketIterator.hasNext()) {
			socket = socketIterator.next();
			if (ServerConnThread.socketHashMapID.get(socket) != null) {
				try{
					socket.close();
				}
				catch(IOException ioe){
					ioe.printStackTrace();
				}
			}
		}
	}

	private class SocketServerThread extends Thread {
		int userid;
		@Override
		public void run() {
			try {
				myserverSocket = new ServerSocket(socketServerPORT);
				ServerOn = true;
				while(ServerOn)
				{
					try
					{
						// Accept incoming connections.
						final Socket clientSocket = myserverSocket.accept();
						// accept() will block until a client connects to the snake_game.
						// If execution reaches this point, then it means that a client
						// socket has been accepted.
						// For each client, we will start a service thread to
						// service the client requests. This is to demonstrate a
						// Multi-Threaded snake_game. Starting a thread also lets our
						// MultiThreadedSocketServer accept multiple connections simultaneously.
						// Start a Service thread
						if(!allplayersjoined) //check to see if all the clients have connected
						{
							HostFragment.userID = HostFragment.userID + 1;	//get next user ID for each clients
							userid = HostFragment.userID;
							Serverlistenerthread serverthread = new Serverlistenerthread(clientSocket);
							serverthread.start();
							testmessage = context.getString(R.string.clientConnAck);
							Serversenderthread serverthread2 = new Serversenderthread(clientSocket, testmessage);
							serverthread2.start();

							//username will be replaced in listner thread
							socketHashMapID.put(clientSocket,userid);

							if (socketHashMapID.size() == HostFragment.numberPlayers) {
								allplayersjoined = true;
								HostFragment.isAllPlayersConnected = true;
							}
						}
					}
					catch(IOException ioe)
					{
						//System.out.println("Exception encountered on accept. Ignoring. Stack Trace :");
						ioe.printStackTrace();
					}
				}
				try
				{
					myserverSocket.close();
					ServerOn = false;
					//System.out.println("ServerConnThread Stopped");
				}
				catch(Exception ioe)
				{
					//System.out.println("Problem stopping snake_game socket");
					System.exit(-1);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void sendToAll(Object gameObject) {
		Iterator<Socket> socketIterator = ServerConnThread.socketHashMapID.keySet().iterator();
		Socket socket;
		while (socketIterator.hasNext()) {
			socket = socketIterator.next();
			if (!ServerConnThread.socketHashMapID.get(socket).equals(((SnakeCommBuffer) gameObject).userID)) {
				Serversenderthread sendGame = new Serversenderthread(socket, gameObject);
				sendGame.start();
			}

			try {
				Thread.sleep(5);   //TODO: initially 100, changed now. verify if needed
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

	public static void sendToClient(Object gameObject) {
		Iterator<Socket> socketIterator = ServerConnThread.socketHashMapID.keySet().iterator();
		Socket socket;
		while (socketIterator.hasNext()) {
			socket = socketIterator.next();
			if (ServerConnThread.socketHashMapID.get(socket).equals(((SnakeCommBuffer) gameObject).userID)) {
				Serversenderthread sendGame = new Serversenderthread(socket, gameObject);
				sendGame.start();
			}
		}
	}

	//TODO: Function to return IP address.
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
						ip += "ServerConnThread running at : "
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
