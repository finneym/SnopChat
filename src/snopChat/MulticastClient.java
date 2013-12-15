package snopChat;


/**
 * Name1: Thea Johnson StudentNumber1:12307926
 * Name2: Max Finney StudentNumber2:12307451
 * Name3 Yana Kulizhskaya StudentNumber3:12300762
 */
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;

import tcdIO.*;

/**
 * Client 
 * Skeleton code for Multicast client
 */
public class MulticastClient extends Thread{

	public static final String MCAST_ADDR = "230.0.0.1"; // hardcoded address for the multicast group
	public static final int MCAST_PORT = 9013; // hardcoded port number for the multicast group
	public static final int DATA_PORT = 50001;  
	public static final int DEFAULT_ID = -1;
	public static final int MAX_BUFFER = 1024; // maximum size for data in a packet  


	ArrayList<Details> receivingFrom;

	MulticastSocket multiSocket;
	DatagramSocket dataSocket;
	InetAddress address;
	int port;
	Terminal terminal = new Terminal();
	int mID;
	/**
	 * Default Constructor
	 * 
	 * Fills an instance with the hardcoded values
	 */
	public MulticastClient() {
		this(MCAST_ADDR, MCAST_PORT, DATA_PORT, DEFAULT_ID);
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
	public MulticastClient(String addr, int port, int dataPort, int id) {
		try {
			this.mID = id;
			this.port= port;
			address = InetAddress.getByName(addr);
			multiSocket = new MulticastSocket(port);
			dataSocket =new DatagramSocket(dataPort);
			multiSocket.joinGroup(address);
			terminal.setTitle("Client   Port - " + this.port + "  Address - " + address.toString());
			//socket.setLoopbackMode(true);
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
		/*		String msg = "Date?";
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
		}*/
		receiveThing();
	}

	/**
	 * receives packet. Will not allow multiple copies of same packet to be taken in
	 * 
	 */
	private void receiveThing(){
		byte[] data; 
		DatagramPacket packet; 
		int size = 0; 
		byte seqNo;  
		Buffer[] buffers; 
		int idNum; 
		buffers = new Buffer[10]; 
		boolean foundIDNum, allFin; 
		allFin=false; 


		terminal.println("Waiting for incoming packets"); 
		while(!allFin){  

			try { 
				data= new byte[MAX_BUFFER+1];  // receive first packet with size of image as payload 
				packet= new DatagramPacket(data, data.length); 

				//will need to change all this if port is not individual to sender
				foundIDNum=false; 
				multiSocket.receive(packet);//receive packet 
				String msg = new String(data, 0, packet.getLength());
				if(msg.substring(0, 7).equals("details")){
					this.receivingFrom.add(this.receiveDetails(packet));
					sendACK(packet.getData()[0], this.receivingFrom.get(this.receivingFrom.size()-1).getNodeId());
				}
				else{
					terminal.println("Received: " + new String(data, 0, packet.getLength()));
					//temp fix as was receiving ACK's up here... need to work out why and possibly come up with better fix
					//if(packet.getLength()>1){
					idNum = packet.getData()[1]; 
					int bufferCount=0; 
					//check if one or more packets have being received from this port before
					while(bufferCount<buffers.length && buffers[bufferCount]!=null && foundIDNum==false){
						if(idNum == buffers[bufferCount].getID()){
							foundIDNum=true;
						}
						else if(!foundIDNum){
							bufferCount+=1;
						}
					}
					/*while(bufferCount<buffers.length && foundPortNum ==false && buffers[bufferCount]!=null){ 
						if(buffers[bufferCount]!=null){ 
							if(buffers[bufferCount].getPortNum() == portNum){ 
								foundPortNum=true; 
							} 
							else if(!foundPortNum){ 
								bufferCount++; 
							} 
						} 
					} */
					//if no packet has being received before
					if(!foundIDNum){ 
						//create new instance of buffer
						buffers[bufferCount]= new Buffer(packet.getPort()); 
						seqNo = data[0]; //get seq num
						data= packet.getData();// reserve buffer to receive image 
						terminal.println("reveived seqNo "+ seqNo +" expected seqNo "+buffers[bufferCount].getExpSeqNum()+" "+ packet.getPort());  
						if(buffers[bufferCount].checkSeqNum(seqNo)&&buffers[bufferCount].checkID(idNum)){ //check if it really is the first packet
							size= (Integer.valueOf(new String(data, 1, packet.getLength()-1))).intValue();  //add size
							terminal.println("Filesize:" + size +" sequence number "+seqNo); 
							buffers[bufferCount].createBuffer(size); 
							buffers[bufferCount].moveOnSeqNum(); // move on the expected seq num
						}
						sendACK((byte)(buffers[bufferCount].getExpSeqNum()), idNum); // send an ack for the next packet expected from this port
					} 

					//otherwise if a packet has being received
					else{ 
						seqNo = data[0];  
						terminal.println("recieved seqNo "+ seqNo +" expected seqNo "+buffers[bufferCount].getExpSeqNum() +"  "+ packet.getPort()); 
						//if this packet is the next packet expected for this port add it to the array
						if(buffers[bufferCount].checkSeqNum(seqNo)&&buffers[bufferCount].checkID(idNum)){ //check it is the right packet
							terminal.println("Received packet - Port: " + packet.getPort() + " - Counter: " + buffers[bufferCount].getCounter() + " - Payload: "+(packet.getLength()-1));    

							buffers[bufferCount].copyIn(packet, data); 
							buffers[bufferCount].counterIncrease((packet.getLength()-1)) ; 
							buffers[bufferCount].moveOnSeqNum(); 
							buffers[bufferCount].checkFin(); 
						} 
						sendACK((byte)(buffers[bufferCount].getExpSeqNum()), idNum); //send an ack for the next packet expected

					}
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

	/**
	 * sends an ack to the port that it got the packet from 
	 * will send the number of the next packet it expects to get
	 * @param sequence number to send in ack
	 * @param packet received
	 */
	private void sendACK(byte seqNo, int id){ 
		int detailID;
		for(detailID=0; detailID<this.receivingFrom.size(); detailID++){
			if(this.receivingFrom.get(detailID).getNodeId()==id){
				break;
			}
		}
		if(detailID==this.receivingFrom.size()-1 && id != this.receivingFrom.get(detailID).getNodeId()){
			System.out.println("Didn't find id in receivingFrom in the sendACK() id- "+id);
			System.exit(-1);
		}
		byte[] ACK; 
		ACK = new byte[1]; 
		DatagramPacket ACKpacket; 
		try { 
			ACK[0] =  (seqNo); 
			ACKpacket = new DatagramPacket(ACK, ACK.length, this.receivingFrom.get(detailID).getmAddressInet(), this.receivingFrom.get(detailID).getServerPort()); 
			dataSocket.send(ACKpacket); 
			terminal.println("ACK "+seqNo+" sent " +ACK); 
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
			multiSocket.receive(packet);
			String msg = new String(buffer, 0, packet.getLength());
			//System.out.println("Sent - "+msg);
			terminal.println("Received - "+msg);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;

	}

	public Details receiveDetails(DatagramPacket packet){
		String[] details = new String(packet.getData()).split("/");										//details[0] "details"
		return new Details(Integer.parseInt(details[3]), 0, Integer.parseInt(details[2]), details[1]);	//details[1] "localhost"
		//details[2] port
		//details[3] id
	}
}

/**
 * Main method
 * Start a client by creating an instance of the class MulticastClient.
 * 
 * @param args 	[0] IP address the client should send to 
 * 				[1] Port number the client should send to
 */
//	public static void main(String[] args) {
//
//		int port= 0;
//		String address=null;
//		MulticastClient client=null;
//
//		System.out.println("Program start");
//		try {
//			if (args.length==2) {
//				address= args[0];
//				port= Integer.parseInt(args[1]);
//
//				client= new MulticastClient(address, port);
//			}
//			else
//				client= new MulticastClient();
//
//			client.run();
//		}	
//		catch(Exception e) {
//			e.printStackTrace();
//			System.exit(-1);
//		}
//		System.out.println("Program end");
//	}
//}
