package snopChat;


/**
 * Name1: Thea Johnson StudentNumber1:12307926
 * Name2 Max Finney StudentNumber2:12307451
 * Name3 Yana Kulizhskaya StudentNumber3:12300762
 */
//import tcdIO.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;

import tcdIO.*;
/**
 * Server 
 * Skeleton code for Multicast server
 */
public class MulticastServer extends Thread{

	public static final String MCAST_ADDR = "230.0.0.1";	// Hardcoded address for the multicast group
	public static final int MCAST_PORT = 9013; 				// Hardcoded port number for the multicast group

	public static final int MAX_BUFFER = 1024; 				// Maximum size for data in a packet
	ArrayList<Node> nodeList;
	MulticastSocket socket;
	InetAddress address;
	Terminal terminal = new Terminal();
	int port;
	

	static final int MTU = 1500; // thea from other project
	static final String FILENAME = "image/input.jpg";

	/**
	 * Default Constructor
	 * 
	 * Fills an instance with the hardcoded values
	 */
	public MulticastServer() {
		this(MCAST_ADDR, MCAST_PORT);
	}

	/**
	 * Constructor
	 * 
	 * Creates an instance with specific values for the 
	 * address and port of the multicast group 
	 * 
	 * @param addr Address of the multicast group as string
	 * @param port Port number of the server 
	 */
	public MulticastServer(String addr, int port) {
		try {
			this.port= port;
			address = InetAddress.getByName(addr);
			socket = new MulticastSocket(port);
			socket.joinGroup(address);
			terminal.setTitle("Server  Port - " + this.port + "  Address - " + address.toString());
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Run method
	 *
	 * This method is continuously looking to receive messages from clients.
	 * The method will reply with a message containing the current date information
	 * if a client sends a message that contains the string "Date?". 
	 */
	public void run() {
		//terminal.println("Testing");
		DatagramPacket packet= null;
		byte[] buffer= null;
		String msg= null;

		try {
			while (true) {
				//System.out.println("Waiting");
				terminal.println("Waiting");

				// receive message from client
				buffer = new byte[MAX_BUFFER];
				packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
				msg= new String(buffer, 0, packet.getLength());
				//System.out.println("Received: " + msg);
				//System.out.println("From: "+packet.getAddress()+":"+packet.getPort());
				terminal.println("Received: " + msg);
				terminal.println("From: "+packet.getAddress()+":"+packet.getPort());

				if (msg.equalsIgnoreCase("Date?")) {
					// send reply to everyone
					msg = new Date().toString();
					buffer = msg.getBytes();
					packet = new DatagramPacket(buffer, buffer.length, address, port);
					//System.out.println("Sending: " + new String(buffer));
					terminal.println("Sending: " + new String(buffer));
					socket.send(packet);
				}
				else if(msg.substring(0,4).equalsIgnoreCase("hello")) {
					// send intro to everyone
					//msg.substring(5, msg.length()) is the name...hopefully
					boolean inList = false;
					for(int i = 0; i<nodeList.size(); i++){
						if(nodeList.get(i).getName().equals(msg.substring(5, msg.length()))){
							inList = true;
							break;
						}
					}
					if(inList==false){
						nodeList.add(new Node(msg.substring(5, msg.length()),packet.getAddress(), packet.getPort()));		
						//System.out.println(msg.substring(5, msg.length()) + " was added");		
						terminal.println(msg.substring(5, msg.length()) + " was added");
					}
				}
			}


		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendThings(){
		byte[] data= null;
		byte[] data2= null;
		DatagramPacket packet= null;

		File file= null;
		FileInputStream fin= null;
		byte[] buffer= null;
		int size;
		int counter;
		byte seqNo;
		byte maxSeqNo = 15;

		try {	
			file= new File(FILENAME);				// Reserve buffer for length of file and read file
			buffer= new byte[(int) file.length()];
			fin= new FileInputStream(file);
			size= fin.read(buffer);
			if (size==-1) throw new Exception("Problem with File Access");
			terminal.println("File size: " + buffer.length + ", read: " + size);

			seqNo =0;
			data= (Integer.toString(size)).getBytes();  // 1st packet contains the length only
			data2 = new byte[data.length +1]; //create a new data array
			data2[0] = seqNo; // add a sequence number to it
			for(int i=1; i<data2.length; i++){ //copy old array into rest of new array
				data2[i]=data[i-1];
			}
			packet= new DatagramPacket(data2, data2.length, address, port);
			socket.send(packet);
			recieveACK(seqNo, packet);

			counter= 0;
			do {
				if(seqNo == maxSeqNo){
					seqNo = 0;
				}
				else{
					seqNo++;
				}
				data= new byte[(counter+MTU<size) ? MTU+1 : size-counter+1];  // The length of the packet is either MTU or a remainder
				data[0]=seqNo;
				java.lang.System.arraycopy(buffer, counter, data, 1, (data.length-1));
				terminal.println("Counter: " + counter + " - Payload size: " + (data.length-1) +" sequence number: "+seqNo+" port " +socket.getLocalPort());

				packet= new DatagramPacket(data, data.length, address, port);
				socket.send(packet);
				//recieveACK(seqNo, packet);
				counter+= (data.length-1);
			} while (counter<size);

			terminal.println("\nSend complete"+" port " +socket.getLocalPort());
		}
		catch(java.lang.Exception e) {
			e.printStackTrace();
		}	
	}
	
	
	private boolean recieveACK(int ACKNum, DatagramPacket toSend){
		boolean positiveACKRecieved = false;
		byte[] ACK;
		ACK = new byte[2];
		DatagramPacket ACKpacket;
		ACKpacket = new DatagramPacket(ACK, ACK.length);
		try {
			while(!positiveACKRecieved) {
				try {
					socket.setSoTimeout(100);
					socket.receive(ACKpacket);
					positiveACKRecieved = (ACK[0]== ACKNum);
					if(positiveACKRecieved){
						terminal.println("ACK: " + ACK[0] +" recieved"+" port " +socket.getLocalPort());
					}
				} catch (SocketTimeoutException e) {
					terminal.println("Timed out: Still waiting for ACK: " + ACKNum+" port " +socket.getLocalPort());
					socket.send(toSend);
					terminal.println("packet " + ACKNum +" resent"+" port " +socket.getLocalPort());
				}
			}

		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return positiveACKRecieved;
	}

	/**
	 * Main method
	 * Starts a server application by creating an instance of 
	 * the class MulticastServer.
	 * 
	 * @param args  [0] IP address the server should bind to 
	 * 				[1] Port number the server should bind to
	 */
	public static void main(String[] args) {
		int port= 0;
		String address=null;
		MulticastServer server=null;

	System.out.println("Program start");
//		Terminal terminal =new Terminal();
//		terminal.println("Program start");
		try {
			if (args.length==2) {
				address= args[0];
				port= Integer.parseInt(args[1]);

				server= new MulticastServer(address, port);
			}
			else
				server= new MulticastServer();

			server.run();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println("Program end");
//		terminal.println("program end");
	}

}
