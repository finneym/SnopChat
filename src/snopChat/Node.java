package snopChat;

import java.io.File;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

//class that should be sending and receiving things and blah blah blah
public class Node {
	public static final String MCAST_NAME ="Kyle"; //hardcoded name for the multicast group
	public static final String MCAST_ADDR = "230.0.0.1"; // hardcoded address for the multicast group
	public static final int MCAST_PORT = 9013; // hardcoded port number for the multicast group

	public static final int MAX_BUFFER = 1024; // maximum size for data in a packet      

	MulticastSocket socket;
	InetAddress mAddress;

	MulticastClient mClient;
	MulticastServer mServer;
	int mPort;
	String mName;
	String mId;
	boolean fileToSend;
	//port num for datagram socket
	public Node(boolean toSend, String message, int portNum, int portNum2){
		this(MCAST_NAME, MCAST_ADDR, MCAST_PORT, portNum, portNum2);
		this.fileToSend =toSend;
		mId=message;
	}
	
	public Node(String name, String address, int port, int dataPort, int dataPort2){
		try{
		this.mName = name;
		this.mAddress = InetAddress.getByName(address);
		this.mPort = port;
		mClient = new MulticastClient(address.toString(), port, dataPort);
		mServer = new MulticastServer(address.toString(), port, dataPort2);
		}catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public void introduce(){
		if(this.fileToSend){
			Thread server = new Thread(mServer.sendMessage(mId));
			server.start();
		}
		Thread client = new Thread(mClient.receiveMessage());
		client.start();
	}
	
	public void send(File file){
//		Thread[] thread = new Thread[2];
//		
//		thread[0] = new Thread(mServer);
//		thread[1] = new Thread(mClient);
//		for(int i = 0; i<thread.length;i++){
//			thread[i].start();
//		}
		if(this.fileToSend){
			mServer.start();
		}
		mClient.start();
	}
	public void send(){
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
	public static void main(String[] args) {
			Node test = new Node(true ,"1", 50002, 50003);
			//Node test2 = new Node(false, "2");
			File file = null;
			test.introduce();
			test.send();
			//test2.introduce();
			//test2.send();
	}
}

