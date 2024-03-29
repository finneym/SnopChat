package snopChat;


/**
 * Name1: Thea Johnson StudentNumber1:12307926
 * Name2 Max Finney StudentNumber2:12307451
 * Name3 Yana Kulizhskaya StudentNumber3:12300762
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import tcdIO.*;

/**
 * Server 
 * Skeleton code for Multicast server
 */
public class MulticastServer{

	public static final String MCAST_ADDR = "230.0.0.1";	// Hardcoded address for the multicast group
	public static final int DATA_PORT = 50002; 
	public static final int DEFAULT_ID = -1;
	public static final int MAX_BUFFER = 1024; 				// Maximum size for data in a packet
	public static final int MAX_SEQ = 126;

	ArrayList<Integer> subscrNodes; //contains ids of all subscribed nodes

	/*used threads*/
	public Runnable sendHello;
	public Runnable getHello;
	public Runnable sendStuff;

	MulticastSocket multiSocket;
	DatagramSocket dataSocket;
	InetAddress address;
	Terminal terminal = new Terminal();
	int port;
	int mID;
	private String fileName;

	static final String FILENAME = "input.jpg";
	static final int HELLO_SIZE=7;


	/**
	 * Constructor
	 * 
	 * Creates an instance with specific values for the 
	 * address and port of the multicast group 
	 * 
	 * @param addr Address of the multicast group as string
	 * @param port Port number of the server 
	 */
	public MulticastServer(String addr, int port, int dataPort, int id) {
		try {
			this.mID = id;
			this.port= port;
			address = InetAddress.getByName(addr);
			multiSocket = new MulticastSocket(port);
			dataSocket =new DatagramSocket(dataPort);
			multiSocket.joinGroup(address);
		
			terminal.setTitle("Server" + id);
			terminal.setLocation(400, 0);

			subscrNodes= new ArrayList<Integer>();
			fileName = FILENAME;
			
			/*used threads*/
			sendHello=new Runnable(){
				public void run(){
					try {
						MulticastServer.this.sendHello();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};

			getHello=new Runnable(){
				public void run(){
					try{
						MulticastServer.this.receiveHello();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};	

			sendStuff=new Runnable(){
				public void run(){
						MulticastServer.this.sendThings();
				}
			};
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * @return terminal
	 */
	public Terminal getTerminal(){
		return terminal;
	}
	/**
	 * method to put to sleep for 1 second
	 */
	synchronized void sleep() {
		try {this.wait(1000);}catch(Exception e){e.printStackTrace();}
	}

	/**
	 * method that sends out a hello message to everyone subscribed to the multicast address
	 * */
	public Runnable sendHello() throws InterruptedException{
		String msg="hello/" + mID + "/"; // sends 'hello' and node ID
		DatagramPacket packet = new DatagramPacket(msg.getBytes(),msg.length(), address, port);
		while(true){
			try {
				multiSocket.send(packet);
				sleep(); //slow down instead of sending out hellos constantly

			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}	
	}

	/**
	 * method to receive hello messages from everyone subscribed to the multicast address
	 * */
	public Runnable receiveHello() throws IOException{
		byte[] data; 
		DatagramPacket packet;
		data= new byte[HELLO_SIZE];
		packet= new DatagramPacket(data, data.length);
		while(true){
			multiSocket.receive(packet);//receive packet 
			String msg = new String(data, 0, packet.getLength());
			/*check if the received packet is a 'hello' packet*/			
			if(msg.contains("hello")){ 
				String[] info =msg.split("/");
				int nodeID=Integer.parseInt(info[1]);
				/*check if a new node*/
				if(!isIDsbscr(nodeID) && mID!=nodeID){
					System.out.println("ID - " + nodeID + "    mID " + mID);
					subscrNodes.add(nodeID);
				}
			}
		}
	}

	/**
	 * method to check for an ID in the subscribedNodes array
	 */
	public boolean isIDsbscr(int id){
		if(subscrNodes.size()!=0){
			for(int i=0;i<subscrNodes.size();i++){
				if(subscrNodes.get(i).equals(id)){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * sets file name to a passed in name
	 */
	public void setFileName(String name){
		fileName=name;
	}
	/**
	 * from working version of stop and wait
	 * will send a file in packets and wait for the correct ACK before sending the next packet
	 * @param name of file to send
	 */
	private void sendThings(){
		byte[] data= null; //original array to read image into
		byte[] data2= null; //array to copy original array into to add sequence number to the beginning
		DatagramPacket packet= null; 

		File file= null; 
		FileInputStream fin= null; 
		byte[] buffer= null; 
		int size; //size of the image
		int counter; //to check when all of image is sent
		byte seqNo; //number to send with packet in first byte of data2
		byte maxSeqNo = MAX_SEQ; 

		try {    
			Thread.sleep(2500);
			//reading in the file
			file= new File(fileName);               // Reserve buffer for length of file and read file 
			buffer= new byte[(int) file.length()]; 
			fin= new FileInputStream(file); 
			size= fin.read(buffer); 
			if (size==-1) throw new Exception("Problem with File Access"); 
			terminal.println("File size: " + buffer.length + ", read: " + size); 

			seqNo =0; //set sequence number to equal 0 to begin (if changeing remember to change expected seq number in the buffer class

			sendDetails(seqNo, maxSeqNo);	
			seqNo++;

			data= (Integer.toString(size)).getBytes();  // 1st packet contains the length only 
			data2 = new byte[data.length +2]; //create a new data array of length one greater than the origional
			data2[0] = seqNo; // add a sequence number to the first byte of this new array
			data2[1] = (byte) this.mID;
			for(int i=2; i<data2.length; i++){ //copy old array into rest of new array 
				data2[i]=data[i-2]; 
			} 
			packet= new DatagramPacket(data2, data2.length, address, port); //create a new packet from the data2 array
			multiSocket.send(packet); 
			recieveACK(seqNo, packet,maxSeqNo);//will not proceed unless correct ACK is received 

			counter= 0; 
			do { 
				//update sequence number to send
				if(seqNo == maxSeqNo){ 
					seqNo = 0; 
				} 
				else{ 
					seqNo++; 
				} 

				data= new byte[(counter+MAX_BUFFER<size) ? MAX_BUFFER+2 : size-counter+2];  // The length of the packet is either MTU or a remainder 
				data[0]=seqNo; //data 0 is the sequence number
				data[1]=(byte)this.mID;
				java.lang.System.arraycopy(buffer, counter, data, 2, (data.length-2)); //copy data into buffer leaving out the sequence number
				terminal.println("Counter: " + counter + " - Payload size: " + (data.length-2) +" sequence number: "+seqNo+" port " +multiSocket.getLocalPort()); 

				packet= new DatagramPacket(data, data.length, address, port); //creat new packet
				multiSocket.send(packet); //send it
				recieveACK(seqNo, packet, maxSeqNo);//will not proceed unless correct ACK is received 
				counter+= (data.length-2); //add the length of the packet sent (not counting the sequence number) to the counter

			} while (counter<size); // while not all image sent
			Thread.sleep(10000);
			receiveDeleted();

			terminal.println("\nSend complete"+" port " +multiSocket.getLocalPort()); 
		} 
		catch(java.lang.Exception e) { 
			e.printStackTrace(); 
		}        
	}
	
/**
 *  method to receive the deletion ACKs.
 */
	public void receiveDeleted() throws IOException{
		byte[] deleted = new byte[9];
		DatagramPacket delPack=new DatagramPacket(deleted, deleted.length);
	
		int[] allDel = new int[subscrNodes.size()];
		
		boolean receivedBefore;
		boolean allDelRecieved=false;

		do{
			try { 
					dataSocket.setSoTimeout(1000); //set time out time
					while(true){
						dataSocket.receive(delPack); //receive packet
						deleted=delPack.getData();
						String msg=new String(deleted, 0, delPack.getLength());

						if(msg.contains("deleted")){
							receivedBefore=false;
							String[] info =msg.split("/");
							int id=Integer.parseInt(info[1]);
							int i;
							for(i=0; i<allDel.length && allDel[i]!=0 && !receivedBefore; i++){
								if(allDel[i]==id)
									receivedBefore=true;
							}
							
							if(allDel[i]!=0){
								terminal.println("Deletion ACK for " + id +" received");
							}
							if(!receivedBefore && i<allDel.length)
							allDel[i]=id;
							boolean matchFound=false;
							allDelRecieved=true; //assume for loop condition
							//try to match every subscribed node's id to a allDel id; if false keep waiting for deletion ACKs
							for(i=0;i<subscrNodes.size() && allDelRecieved;i++){			
								for(int j=0;j<allDel.length&&!matchFound;j++){
									if(subscrNodes.get(i)==allDel[j]){
										matchFound=true;
									}
								}
								if(!matchFound) {
									allDelRecieved=false;
								}
							}
						}
					}
				} catch (SocketTimeoutException e) { 
					//if timeout resent packet and print details
					String notReceivedMsg = "notDeleted/"+this.mID;
					DatagramPacket notReceived = new DatagramPacket(notReceivedMsg.getBytes(), notReceivedMsg.length(), address, port);
					terminal.println("Timed out: Still waiting for Deletions " + "port " + multiSocket.getLocalPort()); 
					multiSocket.send(notReceived); 
					terminal.println("Asked for Deletion ACKs"+" port " +multiSocket.getLocalPort()); 
					} 
			}while(!allDelRecieved);
		} 


	/**
	 * function to receive ACKs from all subscribed nodes
	 * @param sequence number of packet just sent
	 * @param packet just sent
	 * @param maxSeqNum
	 * @return true if correct ack received, false otherwise
	 */
	private boolean recieveACK(int ACKNum, DatagramPacket toSend, int maxSeqNum){ 
		boolean positiveACKRecieved = false; //changes when correct ACK is received
		byte[] ACK= new byte[1]; 
		DatagramPacket ACKpacket; //new packet to receive ack
		ACKpacket = new DatagramPacket(ACK, ACK.length); 
		DatagramPacket[] allACKs = new DatagramPacket[subscrNodes.size()];
	
		boolean receivedBefore;
		boolean allPositiveACKRecieved=false;

		try { 
			while(!allPositiveACKRecieved) { //while a positive ack is not received
				try { 
					do{
						ACKpacket = new DatagramPacket(ACK, ACK.length);
						dataSocket.setSoTimeout(100); //set time out time
						dataSocket.receive(ACKpacket); //receive packer
						System.out.println("ACKPacket -" + ACK[0] + "  Server Num - " + this.mID);
					}while(!ACKpacket.getAddress().equals(InetAddress.getLocalHost()));		//loops until the received item is not from the local host
					positiveACKRecieved = (ACK[0]== (ACKNum==maxSeqNum? 0:ACKNum+1));// if ack number received is equal to the number sent +1 (0 if number sent was max number) 
					if(positiveACKRecieved){ //if it was the right ack 
						receivedBefore=false;;
						int i;
						for(i=0; i<allACKs.length && allACKs[i]!=null; i++){
							//if(allACKs[i].equals(ACKpacket)){
							if(allACKs[i].getAddress().equals(ACKpacket.getAddress()) && allACKs[i].getPort()== ACKpacket.getPort()){
								receivedBefore=true;
								break;
							}
						}
						terminal.println("ACK: " + ACK[0] +" recieved "+i);
						if(!receivedBefore && i<allACKs.length){
							allACKs[i]=ACKpacket;
						}
						allPositiveACKRecieved=allACKsReceived(allACKs);
					} 
				} catch (SocketTimeoutException e) { 
					//if timeout resent packet and print details
					terminal.println("Timed out: Still waiting for ACK: " + ACKNum+" port " +multiSocket.getLocalPort()); 
					multiSocket.send(toSend); 
					terminal.println("packet " + ACKNum +" resent"+" port " +multiSocket.getLocalPort()); 
				} 
			} 

		}  
		catch (IOException e) { 
			e.printStackTrace(); 
		} 
		return positiveACKRecieved; 
	} 
	/**
	 * a method to count if an ack from all subscribed clients has being received
	 */
	public boolean allACKsReceived(DatagramPacket[] ACKs){
		boolean allReceived=true;
		for(int i=0; i<ACKs.length; i++){
			if(ACKs[i]==null){
				allReceived=false;
			}
		}
		return allReceived;
	}

	/**
	 * a method to send first package containing server info
	 */
	public void sendDetails(int seqNo, int maxSeqNo){
		//details address port id
		try {
			String detailsToSend = "details/"+mID+"/"+this.dataSocket.getLocalPort()+"/"+this.fileName.substring(fileName.length()-3)+"/";
			byte[] data = new byte[detailsToSend.length()+1];
			System.arraycopy(detailsToSend.getBytes(), 0, data, 1, detailsToSend.length());
			data[0] = (byte) seqNo;
			DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
			multiSocket.send(packet);
			recieveACK(seqNo, packet,maxSeqNo);//will not proceed unless correct ACK is received 
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

}
