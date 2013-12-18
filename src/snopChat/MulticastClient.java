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
import java.net.SocketTimeoutException;
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


	ArrayList<Buffer> receivingFrom;

	MulticastSocket multiSocket;
	DatagramSocket dataSocket;
	InetAddress address;
	int port;
	Terminal terminal = new Terminal();
	int mID;
	private String fileFormat;
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
			this.receivingFrom = new ArrayList<Buffer>();
			address = InetAddress.getByName(addr);
			multiSocket = new MulticastSocket(port);
			dataSocket =new DatagramSocket(dataPort);
			multiSocket.joinGroup(address);
			//terminal.setTitle("Client   Port - " + this.port + "  Address - " + address.toString());
			terminal.setTitle("Client " + id);
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
		//Buffer[] buffers; 
		int idNum; 
		//buffers = new Buffer[10]; 
		int receivedBefore;
		boolean allFin; 
		allFin=false; 
		boolean isNotMe = false;

		terminal.println("Waiting for incoming packets"); 
		while(!allFin){  

			try { 
				data= new byte[MAX_BUFFER+2];  // receive first packet with size of image as payload 
				packet= new DatagramPacket(data, data.length); 

				//will need to change all this if port is not individual to sender
				multiSocket.receive(packet);//receive packet


				int tempID = mID;
				String msg = new String(data, 1, packet.getLength()-1);
				try{
					isNotMe = !(data[1]==(byte)mID || Integer.parseInt(msg.split("/")[1])==mID);
				}catch(java.lang.NumberFormatException e){
					if( data[1]!=(byte)mID){
						isNotMe = true;
					}
				}catch(java.lang.ArrayIndexOutOfBoundsException e){
					try{
						if( data[1]!=(byte)mID){
							isNotMe = true;
						}
					}catch(java.lang.ArrayIndexOutOfBoundsException q){
						if(Integer.parseInt(msg.split("/")[1])!=mID){
							isNotMe = true;
						}
					}
				}
				if(isNotMe){
					//temp fix as was receiving ACK's up here... need to work out why and possibly come up with better fix
					if(!msg.contains("ello") && !msg.contains("elete")){
						if(msg.length()>=7 && msg.substring(0, 7).equals("details")){
							int index = this.isInReceiveDetails(packet);
							if(index==-1){
								this.receivingFrom.add(this.receiveDetails(packet));
								receivingFrom.get(this.receivingFrom.size()-1).moveOnSeqNum(); // move on the expected seq num
								sendACK((byte)receivingFrom.get(this.receivingFrom.size()-1).getExpSeqNum(), this.receivingFrom.get(this.receivingFrom.size()-1).getNodeId());
							}
							else{
								this.sendACK((byte)receivingFrom.get(index).getExpSeqNum(), this.receivingFrom.get(index).getID());
							}
						}
						else{
							//terminal.println("Received: " + new String(data, 0, packet.getLength()));
							idNum = packet.getData()[1]; 
							int bufferCount=0; 
							//check if one or more packets have being received from this port before
							//					while(bufferCount<receivingFrom.size() && receivingFrom.get(bufferCount)!=null && receivedBefore==false){
							//						int gotid = this.receivingFrom.get(bufferCount).getID();
							//						int sizeOfList = this.receivingFrom.get(bufferCount).getSize();
							//						if(idNum == receivingFrom.get(bufferCount).getID() && receivingFrom.get(bufferCount).getSize()!=0){
							//							receivedBefore=true;
							//						}
							//						else if(!receivedBefore){
							//							bufferCount+=1;
							//						}
							//					}
							receivedBefore=0; 
							while(bufferCount<receivingFrom.size() && receivingFrom.get(bufferCount)!=null){
								if(idNum == receivingFrom.get(bufferCount).getID() && receivingFrom.get(bufferCount).getSize()!=0){
									receivedBefore=2;
									break;
								}
								else if(idNum == receivingFrom.get(bufferCount).getID() && receivingFrom.get(bufferCount).getSize()==0){
									receivedBefore = 1;
									break;
								}
								else{
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
							if(receivedBefore == 1){ 
								//create new instance of buffer
								//buffers[bufferCount]= new Buffer(packet.getPort()); 
								seqNo = data[0]; //get seq num
								data= packet.getData();// reserve buffer to receive image 
								terminal.println("received seqNo "+ seqNo +" expected seqNo "+receivingFrom.get(bufferCount).getExpSeqNum()+" "+ packet.getPort());  
								if(receivingFrom.get(bufferCount).checkSeqNum(seqNo)&&receivingFrom.get(bufferCount).checkID(idNum)){ //check if it really is the first packet
									size= (Integer.valueOf(new String(data, 2, packet.getLength()-2))).intValue();  //add size
									terminal.println("Filesize:" + size +" sequence number "+seqNo); 
									receivingFrom.get(bufferCount).createBuffer(size); 
									receivingFrom.get(bufferCount).moveOnSeqNum(); // move on the expected seq num
								}
								sendACK((byte)(receivingFrom.get(bufferCount).getExpSeqNum()), idNum); // send an ack for the next packet expected from this port
							} 

							//otherwise if a packet has being received
							else if (receivedBefore == 2){ 
								seqNo = data[0];  
								terminal.println("recieved seqNo "+ seqNo +" expected seqNo "+receivingFrom.get(bufferCount).getExpSeqNum() +"  "+ packet.getPort()); 
								//if this packet is the next packet expected for this port add it to the array
								if(receivingFrom.get(bufferCount).checkSeqNum(seqNo)&&receivingFrom.get(bufferCount).checkID(idNum)){ //check it is the right packet
									terminal.println("Received packet - Port: " + packet.getPort() + " - Counter: " + receivingFrom.get(bufferCount).getCounter() + " - Payload: "+(packet.getLength()-2));    

									receivingFrom.get(bufferCount).copyIn(packet, data); 
									receivingFrom.get(bufferCount).counterIncrease((packet.getLength()-2)) ; 
									receivingFrom.get(bufferCount).moveOnSeqNum();
									
									if(receivingFrom.get(bufferCount).checkFin()){
										new Thread(receivingFrom.get(bufferCount).fin()).run();
										sendDeletedACK(idNum);
										while(!deletionWarnReceived()){
											sendDeletedACK(idNum);}
									}
								}
									sendACK((byte)(receivingFrom.get(bufferCount).getExpSeqNum()), idNum); //send an ack for the next packet expected
								
								

							}
						}
					}
					//				else if(msg.contains("notDeleted")){			//this is the message from the server 
					//					int index = this.isInReceiveDetails(packet);	//finds which receiving from
					//					this.receivingFrom.get(index).deletefile();		//attempted to delete the file
					//					this.sendDeletedACK(Integer.parseInt(msg.split("/")[1]));		//sends delete ACK
					//				}
				} 
			}
			catch(java.lang.Exception e) { 
				e.printStackTrace(); 
			}    
			allFin=true; 

			/*fix to stop program finishing after receiving hello... not nicest but works
			 reason it was jumping was because no buffers were created in the array list so 
			 all fin was true and the loop ended*/
			if(receivingFrom.size()==0){
				allFin=false;
			}

			else{
				for(int i=0; i<receivingFrom.size() && receivingFrom.get(i)!=null; i++){ 
					if(!receivingFrom.get(i).getFin() || !this.receivingFrom.get(i).isDeleted()){ 
						allFin=false; 
					} 
				}
			}
			/** for now*/
			if(allFin==true) terminal.println("Program finished.");
			/** was throwing IllegalMonitorStateException, program seems to work the same without it **/
			//			if(allFin == true){
			//				boolean finished = false;
			//				try {
			//					Thread.currentThread().wait(1000);
			//					this.multiSocket.setTimeToLive(1000);
			//				} catch (IOException e) {
			//					finished = true;
			//					terminal.println("Program completed");
			//
			//				} catch (InterruptedException e) {
			//					// TODO Auto-generated catch block
			//					e.printStackTrace();
			//				}
			//				if(!finished){
			//					allFin = false;
			//				}
			//			}

		} 
	}

	// method to wait for any 'not deleted' warnings.
	public boolean deletionWarnReceived() throws IOException{
		try{
			byte[] data= new byte[16];  // receive first packet with size of image as payload 
			DatagramPacket packet= new DatagramPacket(data, data.length); 
			dataSocket.setSoTimeout(1000);
			while(true){
				dataSocket.receive(packet);
				String msg = new String(data, 0, packet.getLength()-1);
				if(msg.contains("notDeleted")){
					return false;
				}
			}
		} catch (SocketTimeoutException e) { 
			return true;
		} 
	}
	/**
	 * sends an ack to the port that it got the packet from 
	 * will send the number of the next packet it expects to get
	 * @param sequence number to send in ack
	 * @param packet received
	 */
	private void sendACK(byte seqNo, int id){ 
		int detailIndex;
		for(detailIndex=0; detailIndex<this.receivingFrom.size(); detailIndex++){
			if(this.receivingFrom.get(detailIndex).getNodeId()==id){
				break;
			}
		}
		if(detailIndex==this.receivingFrom.size()-1 && id != this.receivingFrom.get(detailIndex).getNodeId()){
			//			System.out.println("Didn't find id in receivingFrom in the sendACK() id- "+id);
			terminal.println("Didn't find id in receivingFrom in the sendACK() id- "+id);
			System.exit(-1);
		}
		byte[] ACK; 
		ACK = new byte[1]; 
		DatagramPacket ACKpacket; 
		try { 
			ACK[0] =  (seqNo); 
			ACKpacket = new DatagramPacket(ACK, ACK.length, this.receivingFrom.get(detailIndex).getmAddressInet(), this.receivingFrom.get(detailIndex).getServerPort()); 
			dataSocket.send(ACKpacket); 
			terminal.println("ACK "+seqNo+" sent "); 
		} catch (SocketException e) { 
			e.printStackTrace(); 
		} catch (IOException e) { 
			e.printStackTrace(); 
		} 

	} 

	public void sendDeletedACK(int id){			//a copy of sendACK with a few adjustments
		int detailIndex;
		for(detailIndex=0; detailIndex<this.receivingFrom.size(); detailIndex++){
			if(this.receivingFrom.get(detailIndex).getNodeId()==id){
				break;
			}
		}
		if(detailIndex==this.receivingFrom.size()-1 && id != this.receivingFrom.get(detailIndex).getNodeId()){
			//			System.out.println("Didn't find id in receivingFrom in the sendACK() id- "+id);
			terminal.println("Didn't find id in receivingFrom in the sendACK() id- "+id);
			System.exit(-1);
		}
		String deletedACK = "deleted";
		DatagramPacket delPack = new DatagramPacket(deletedACK.getBytes(), deletedACK.length(), this.receivingFrom.get(detailIndex).getmAddressInet(), this.receivingFrom.get(detailIndex).getServerPort());
		try { 
			dataSocket.send(delPack); 
			terminal.println("Send Deletion");
		} catch (SocketException e) { 
			e.printStackTrace(); 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*public Runnable receiveMessage(){
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

	}*/

	public Buffer receiveDetails(DatagramPacket packet){

		String[] details = new String(packet.getData()).split("/");										//details[0] "details"
		for(int i = 0; i<details.length; i++){
			terminal.println(details[i]);
		}
		
		return new Buffer(Integer.parseInt(details[1]), Integer.parseInt(details[2]), packet.getAddress(), mID, details[3]);	//details[1] "localhost"
		//details[2] port
		//details[3] id
	}
	public int isInReceiveDetails(DatagramPacket packet){
		for(int i = 0; i<this.receivingFrom.size(); i++){
			if(this.receivingFrom.get(i).getNodeId() == Integer.parseInt(new String(packet.getData()).split("/")[1])){
				return i;
			}
		}
		return -1;
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
