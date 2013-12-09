package snopChat;

import java.io.File;
import java.net.InetAddress;
import java.net.MulticastSocket;

//class that should be sending and receiving things and blah blah blah
public class Node {
	public static final String MCAST_ADDR = "230.0.0.1"; // hardcoded address for the multicast group
	public static final int MCAST_PORT = 9013; // hardcoded port number for the multicast group
	
	public static final int MAX_BUFFER = 1024; // maximum size for data in a packet      
	
	MulticastSocket socket;
	InetAddress mAddress;
	
	MulticastClient mClient;
	MulticastServer mServer;
	int mPort;
	String mName;
	public Node(String name, InetAddress address, int port){
		this.mName = name;
		this.mAddress = address;
		this.mPort = port;
		mClient = new MulticastClient(address.toString(), port);
		mServer = new MulticastServer(address.toString(), port);
	}
	public void introduce(){
		this.mClient.sendMessage(getName());
	}
	public void send(File file){
		Thread[] thread = new Thread[2];
		thread[0] = new Thread(mServer);
		thread[1] = new Thread(mClient);
		for(int i = 0; i<thread.length;i++){
			thread[i].start();
		}
	}
	String getName(){
		return this.mName;
	}
}
