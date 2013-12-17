package snopChat;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

//class that should be sending and receiving things and blah blah blah
public class Node {
	public static final String MCAST_NAME ="Kyle"; //hardcoded name for the multicast group
	public static final String MCAST_ADDR = "230.0.0.1"; // hardcoded address for the multicast group

	/*hardcoded multicast ports*/
	public static final int MCAST_PORT1 = 9013;
	public static final int MCAST_PORT2 = 9014;

	public static final int MAX_BUFFER = 1024; // maximum size for data in a packet      

	MulticastSocket socket;
	InetAddress mAddress;

	MulticastClient mClient;
	MulticastServer mServer;
	int mPort;
	String mName;
	int mId;
	boolean fileToSend;
	//port num for datagram socket
	public Node(boolean toSend, int nodeId, int clientPort, int serverPort, int multicastPort){
		this(MCAST_NAME, MCAST_ADDR, multicastPort, clientPort, serverPort, nodeId);
		this.fileToSend =toSend;
	}

	public Node(String name, String address, int port, int clientPort, int serverPort, int nodeId){
		try{
			this.mName = name;
			this.mAddress = InetAddress.getByName(address);
			this.mPort = port;
			this.mId=nodeId;
			mClient = new MulticastClient(address.toString(), port, clientPort, this.mId);
			mServer = new MulticastServer(address.toString(), port, serverPort, this.mId);
		}catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void introduce(){
		new Thread(mServer.getHello).start();
		new Thread(mServer.sendHello).start();
		//		if(this.fileToSend){
		//			new Thread(mServer.sendStuff).start();
		//			
		//		}
		//		Thread client = new Thread(mClient.receiveMessage());
		//		client.start();
	}

	public void send(){
		//		Thread[] thread = new Thread[2];
		//		
		//		thread[0] = new Thread(mServer);
		//		thread[1] = new Thread(mClient);
		//		for(int i = 0; i<thread.length;i++){
		//			thread[i].start();
		//		}
		if(this.fileToSend){
			new Thread(mServer.sendStuff).start();
		}
		mClient.start();
	}
	/**
	 * checks if a file exists and then gets it read to send
	 * @param fileName
	 * @return
	 */
	public boolean inputFile(String fileName){
		File file = new File(fileName);
		if(file.exists()){
			mServer.setFileName(fileName);
			return true;
		}
		System.out.println("Invalid File Name");
		return false;
	}
	
	//	public void send(){
	//		Thread[] thread = new Thread[2];
	//		thread[0] = new Thread(mServer);
	//		thread[1] = new Thread(mClient);
	//		for(int i = 0; i<thread.length;i++){
	//			thread[i].start();
	//		}
	//	}

	//	String getName(){
	//		return this.mName;
	//	}

	public static void main(String[] args) throws InterruptedException, IOException {
		Node test = new Node(true ,1, 50002, 50003, MCAST_PORT1); // true if sending image, nodeID, clientPort, serverPort, mcastPort for node
		Node test2 = new Node(true, 2, 50004, 50005, MCAST_PORT1);
		Node test3 = new Node(false, 3, 50006, 50007, MCAST_PORT1);
		File file = null;
		test.inputFile("banana.jpg");
		test.introduce();
		test.send()	;
		test2.introduce();
		test2.send();	
		test3.introduce();
		test3.send();		
	}
}

