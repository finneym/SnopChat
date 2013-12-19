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
	public static final int DEFAULT_ID = -1;
	public static final int MAX_BUFFER = 1024; // maximum size for data in a packet  
	public static final int DATA_PORT = 50001;
	

	ArrayList<Buffer> receivingFrom;	//stores buffers of all the servers that are sending an image

	MulticastSocket multiSocket;
	DatagramSocket dataSocket;
	InetAddress address;
	int port;
	Terminal terminal;
	int mID;
//	private String fileFormat;


	public MulticastClient(){
		this(MCAST_ADDR, MCAST_PORT, DATA_PORT, DEFAULT_ID);
	}
	/**
	 * Constructor
	 * 
	 * Creates an instance with specific values for the 
	 * address and port of the multicast group 
	 * 
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
			terminal = new Terminal();
			terminal.setTitle("Client " + id);
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}


	/**
	 * Run method
	 *
	 * is called when a client instance is created,
	 * receives incoming data packets
	 */
	public void run(){
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
		int idNum; 
		
		int receivedBefore;
		
		boolean allFin=false; 
		boolean isNotMe = false;

		terminal.println("Waiting for incoming packets"); 
		while(!allFin){  

			try { 
				data= new byte[MAX_BUFFER+2];  // receive first packet with size of image as payload 
				packet= new DatagramPacket(data, data.length); 

				multiSocket.receive(packet);//receive packet
				
				//ensures the packet is not from its own server; if so, discards the data
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
					//check if the packet is either a 'hello' or a 'deleted' packet
					if(!msg.contains("ello") && !msg.contains("elete")){
						//if the very first packet, use the details to create a buffer to store the data
						if(msg.length()>=7 && msg.substring(0, 7).equals("details")){
							int index = this.isInReceiveDetails(packet);
							if(index==-1){
								this.receivingFrom.add(this.receiveDetails(packet));
								receivingFrom.get(this.receivingFrom.size()-1).moveOnSeqNum(); // move on the expected seq num
								sendACK((byte)receivingFrom.get(this.receivingFrom.size()-1).getExpSeqNum(), this.receivingFrom.get(this.receivingFrom.size()-1).getNodeId());
							}
							else{
								this.sendACK((byte)receivingFrom.get(index).getExpSeqNum(), this.receivingFrom.get(index).getID()); //reply with an ACK
							}
						}
						else{
							idNum = packet.getData()[1]; 
							int bufferCount=0; 
							//check if one or more packets have being received from this port before
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
		
							//if no packet has been received before
							if(receivedBefore == 1){ 
								//create new instance of buffer
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
								}
									sendACK((byte)(receivingFrom.get(bufferCount).getExpSeqNum()), idNum); //send an ack for the next packet expected
								
									// if the whole file has been sent, display and send a deletionACK after the file has been deleted
									if(receivingFrom.get(bufferCount).checkFin()){
										new Thread(receivingFrom.get(bufferCount).fin()).run();
										//sleep(10000);
										sendDeletedACK(idNum);
										//wait for a warning from the server if the deletionACK is lost, if warning is received re-send the deletion ACK
										if(deletionWarnReceived()){
											sendDeletedACK(idNum);
										}
									}
							}
						}
					}
				} 
			}
			catch(java.lang.Exception e) { 
				e.printStackTrace(); 
			}    
			allFin=true; 

			/*fix to stop program finishing after receiving hello*/
			if(receivingFrom.size()==0){
				allFin=false;
			}

			else{
				for(int i=0; i<receivingFrom.size() && receivingFrom.get(i)!=null; i++){ 
					if(!receivingFrom.get(i).getFin() ){ 
						allFin=false; 
					} 
				}
			}
			
			if(allFin==true){
				terminal.println("Program finished.");
			}
		} 
	}
	
	/**
	 * function to put to sleep given a sleep time
	 * */
	synchronized void sleep(int time) {
		try {this.wait(time);}catch(Exception e){e.printStackTrace();}
	}

	/**
	 * method to wait for any deletion warning messages from the server
	 * a timer is set and if no warning arrives within the given time
	 * the method terminates
	 */
	public boolean deletionWarnReceived() throws IOException{
		try{
			byte[] data= new byte[16]; 
			DatagramPacket packet= new DatagramPacket(data, data.length); 
			dataSocket.setSoTimeout(1000);
			while(true){	//receive packets until timer runs out
				dataSocket.receive(packet);
				String msg = new String(data, 0, packet.getLength()-1);
				if(msg.contains("notDeleted")){
					return true;
				}
			}
		} catch (SocketTimeoutException e) { 
			return false;
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
	
	/**
	 * sends a 'deleted' ACK to the server that sent the image.
	 * a deletedACK contains 'deleted' and the id of the sending client
	 */

	public void sendDeletedACK(int id){			//a copy of sendACK with a few adjustments
		int detailIndex;
		for(detailIndex=0; detailIndex<this.receivingFrom.size(); detailIndex++){
			if(this.receivingFrom.get(detailIndex).getNodeId()==id){
				break;
			}
		}
		if(detailIndex==this.receivingFrom.size()-1 && id != this.receivingFrom.get(detailIndex).getNodeId()){
			terminal.println("Didn't find id in receivingFrom in the sendACK() id- "+id);
			System.exit(-1);
		}
		String deletedACK = "deleted/"+mID;
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

	/**
	 * receives first packet and uses the details in it to create a new buffer for data storage
	 * @return an instance of Buffer
	 */
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
