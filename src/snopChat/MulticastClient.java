package snopChat;


/**
 * Name1: Thea Johnson StudentNumber1:12307926
 * Name2: Max Finney StudentNumber2:12307451
 * Name3 Yana Kulizhskaya StudentNumber3:12300762
 */
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

import tcdIO.*;

/**
 * Client 
 * Skeleton code for Multicast client
 */
public class MulticastClient extends Thread{
	
	public static final String MCAST_ADDR = "230.0.0.1"; // hardcoded address for the multicast group
	public static final int MCAST_PORT = 9013; // hardcoded port number for the multicast group
	
	public static final int MAX_BUFFER = 1024; // maximum size for data in a packet  
	
	static final int MTU = 1500; // thea from other project
	
	MulticastSocket socket;
	InetAddress address;
	int port;
	Terminal terminal = new Terminal();
	/**
	 * Default Constructor
	 * 
	 * Fills an instance with the hardcoded values
	 */
	public MulticastClient() {
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
	public MulticastClient(String addr, int port) {
		try {
			this.port= port;
			address = InetAddress.getByName(addr);
			socket = new MulticastSocket(port);
			socket.joinGroup(address);
			terminal.setTitle("Client   Port - " + this.port + "  Address - " + address.toString());
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	
	/**
	 * Run method
	 *
	 * This method sends a datagram with the strnig "Data?" to a server and
	 * then enters an endless loop in which it attempts to receive datagrams
	 * and prints the content of received datagrams.
	 */
	public void run(){
		String msg = "Date?";
		byte[] buffer;
		DatagramPacket packet = null;
		
		try {
			
			// send datagram to server - asking for date
			packet = new DatagramPacket(msg.getBytes(),	msg.length(), address, port);
			socket.send(packet);
			//System.out.println("Send Msg");
			terminal.println("Send Msg");
			// wait for incoming datagrams and print their content
			while (true) {
				//System.out.println("Waiting");
				terminal.println("Waiting");
				buffer = new byte[MAX_BUFFER];
				packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
				buffer= packet.getData();
				//System.out.println("Received: " + new String(buffer, 0, packet.getLength()));
				//System.out.println("From: "+packet.getAddress()+":"+packet.getPort());
				terminal.println("Received: " + new String(buffer, 0, packet.getLength()));
				terminal.println("From: "+packet.getAddress()+":"+packet.getPort());
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	private void receiveThing(){
		byte[] data;
		DatagramPacket packet;
		int size = 0;
		byte seqNo;
		Buffer[] buffers;
		int portNum;
		buffers = new Buffer[10];
		boolean foundPortNum, allFin;
		allFin=false;


		terminal.println("Waiting for incoming packets");
		while(!allFin){

			try {
				data= new byte[MTU+1];  // receive first packet with size of image as payload
				packet= new DatagramPacket(data, data.length);

				foundPortNum=false;
				socket.receive(packet);
				portNum = packet.getPort();
				int bufferCount=0;
				while(bufferCount<buffers.length && foundPortNum ==false && buffers[bufferCount]!=null){
					if(buffers[bufferCount]!=null){
						if(buffers[bufferCount].getPortNum() == portNum){
							foundPortNum=true;
						}
						else if(!foundPortNum){
							bufferCount++;
						}
					}
				}

				if(!foundPortNum){

					buffers[bufferCount]= new Buffer(packet.getPort());
					seqNo = data[0];
					data= packet.getData();// reserve buffer to receive image
					terminal.println("reveived seqNo "+ seqNo +" expected seqNo "+buffers[bufferCount].getExpSeqNum()+" "+ packet.getPort());
					terminal.println("Received: " + new String(data, 0, packet.getLength()));
					terminal.println("From: "+packet.getAddress()+":"+packet.getPort());
					sendACK(seqNo, packet);

					if(buffers[bufferCount].checkSeqNum(seqNo)&&buffers[bufferCount].checkPort(packet.getPort())){
						size= (Integer.valueOf(new String(data, 1, packet.getLength()-1))).intValue();
						terminal.println("Filesize:" + size +" sequence number "+seqNo);
						buffers[bufferCount].createBuffer(size);
						buffers[bufferCount].moveOnSeqNum();
					}
				}


				else{
					seqNo = data[0];
					sendACK(seqNo, packet);
					terminal.println("recieved seqNo "+ seqNo +" expected seqNo "+buffers[bufferCount].getExpSeqNum() +"  "+ packet.getPort());

					if(buffers[bufferCount].checkSeqNum(seqNo)&&buffers[bufferCount].checkPort(packet.getPort())){
						terminal.println("Received packet - Port: " + packet.getPort() + " - Counter: " + buffers[bufferCount].getCounter() + " - Payload: "+(packet.getLength()-1));
						terminal.println("Received: " + new String(data, 0, packet.getLength()));
						terminal.println("From: "+packet.getAddress()+":"+packet.getPort());	

						buffers[bufferCount].copyIn(packet, data);
						buffers[bufferCount].counterIncrease((packet.getLength()-1)) ;
						buffers[bufferCount].moveOnSeqNum();
					}

					buffers[bufferCount].checkFin();

				}
			}

			catch(java.lang.Exception e) {
				e.printStackTrace();
			}	
			allFin=true;
			for(int i=0; i<buffers.length && buffers[i]!=null; i++){
				if(!buffers[i].getFin()){
					allFin=false;
				}
			}
		}
		terminal.println("Program completed");	
	}
	
	private void sendACK(byte seqNo, DatagramPacket revived){
		byte[] ACK;
		ACK = new byte[1];
		DatagramPacket ACKpacket;
		try {
			ACK[0] = seqNo;
			ACKpacket = new DatagramPacket(ACK, ACK.length, revived.getAddress(), revived.getPort());
			socket.send(ACKpacket);
			terminal.println("ACK "+seqNo+" sent " +"port " + ACKpacket.getPort());
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public Runnable receiveMessage(){
		byte[] buffer = new byte[MAX_BUFFER];
		DatagramPacket packet = new DatagramPacket(buffer,	buffer.length, address, port);
		try {
			socket.receive(packet);
			String msg = new String(buffer, 0, packet.getLength());
			//System.out.println("Sent - "+msg);
			terminal.println("Received - "+msg);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
		
	}
	
	/**
	 * Main method
	 * Start a client by creating an instance of the class MulticastClient.
	 * 
	 * @param args 	[0] IP address the client should send to 
	 * 				[1] Port number the client should send to
	 */
	public static void main(String[] args) {

		int port= 0;
		String address=null;
		MulticastClient client=null;
		
		System.out.println("Program start");
		try {
			if (args.length==2) {
				address= args[0];
				port= Integer.parseInt(args[1]);
				
				client= new MulticastClient(address, port);
			}
		else
			client= new MulticastClient();
		
		client.run();
		}	
		catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println("Program end");
	}
}
