package snopChat;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**class that should be sending and receiving things**/
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

	/**
	 * default Constructor for Node class
	 *
	 */
	public Node(boolean toSend, int nodeId, int clientPort, int serverPort, int multicastPort){
		this(MCAST_NAME, MCAST_ADDR, multicastPort, clientPort, serverPort, nodeId);
		this.fileToSend =toSend;
	}
	
	/**
	 * Constructor
	 *
	 * creates an instance of node, which is capable of sending and receiving images
	 */
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

	/**
	 * server starts sending out and receiving hello packets
	 */
	public void introduce(){
		new Thread(mServer.getHello).start();
		new Thread(mServer.sendHello).start();
	}

	/**
	 * method that initialises a thread for the server if there's an image to be sent
	 * and a client thread for receiving data
	 */
	public void send(){
		if(this.fileToSend){
			new Thread(mServer.sendStuff).start();
		}else{
			mServer.getTerminal().setVisible(false);
		}
		new Thread(mClient).start();
	}
	/**
	 * checks if a file exists and then gets it read to send
	 * @param fileName
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
	
	/**
	 * main method
	 * 
	 * initialises nodes and calls functions that send and receive data
	*/

	public static void main(String[] args) throws InterruptedException, IOException {
		Node test = new Node(true ,1, 50002, 50003, MCAST_PORT1); // true if sending image, nodeID, clientPort, serverPort, mcastPort for node
		Node test2 = new Node(true, 2, 50004, 50005, MCAST_PORT1);
		Node test3 = new Node(false, 3, 50006, 50007, MCAST_PORT1);
		
		test.inputFile("banana.jpg");
		
		test.introduce();
		test.send()	;
		test2.introduce();
		test2.send();	
		test3.introduce();
		test3.send();		
	}
}

