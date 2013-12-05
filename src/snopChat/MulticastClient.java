package snopChat;


/**
 * Name1: Thea Johnson StudentNumber1:12307926
 * Name2: Max Finney StudentNumber2:12307451
 * Name3 Yana Kulizhskaya StudentNumber3:12300762
 */
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;


/**
 * Client 
 * Skeleton code for Multicast client
 */
public class MulticastClient extends Thread{
	
	public static final String MCAST_ADDR = "230.0.0.1"; // hardcoded address for the multicast group
	public static final int MCAST_PORT = 9013; // hardcoded port number for the multicast group
	
	public static final int MAX_BUFFER = 1024; // maximum size for data in a packet      
	
	MulticastSocket socket;
	InetAddress address;
	int port;
	
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
			System.out.println("Send Msg");
			
			// wait for incoming datagrams and print their content
			while (true) {
				System.out.println("Waiting");
				
				buffer = new byte[MAX_BUFFER];
				packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
				buffer= packet.getData();
				System.out.println("Received: " + new String(buffer, 0, packet.getLength()));
				System.out.println("From: "+packet.getAddress()+":"+packet.getPort());
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
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
