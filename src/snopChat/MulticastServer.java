package snopChat;


/**
 * Name1: Thea Johnson StudentNumber1:12307926
 * Name2 Max Finney StudentNumber2:12307451
 * Name3 Yana Kulizhskaya StudentNumber3:12300762
 */

import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Date;

/**
 * Server 
 * Skeleton code for Multicast server
 */
public class MulticastServer {

	public static final String MCAST_ADDR = "230.0.0.1";	// Hardcoded address for the multicast group
	public static final int MCAST_PORT = 9013; 				// Hardcoded port number for the multicast group

	public static final int MAX_BUFFER = 1024; 				// Maximum size for data in a packet

	MulticastSocket socket;
	InetAddress address;
	int port;

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
		DatagramPacket packet= null;
		byte[] buffer= null;
		String msg= null;

		try {
			while (true) {
				System.out.println("Waiting");

				// receive message from client
				buffer = new byte[MAX_BUFFER];
				packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
				msg= new String(buffer, 0, packet.getLength());
				System.out.println("Received: " + msg);
				System.out.println("From: "+packet.getAddress()+":"+packet.getPort());

				if (msg.equalsIgnoreCase("Date?")) {
					// send reply to everyone
					msg = new Date().toString();
					buffer = msg.getBytes();
					packet = new DatagramPacket(buffer, buffer.length, address, port);
					System.out.println("Sending: " + new String(buffer));
					socket.send(packet);
				}
			}
			//Just part of the code to send a image without any protocols
/*			File file = new File("input.jpg");
			buffer = new byte[(int) file.length()];
			FileInputStream fin = new FileInputStream(file);
			int size = fin.read(buffer);
			if (size==-1) throw new Exception("Problem with File Access");
			System.out.println("File size: " + buffer.length + ", read: " + size);
			byte[] data = new byte[(Integer.toString(size)).getBytes().length];
			java.lang.System.arraycopy((Integer.toString(size)).getBytes(), 0, data, 0, (Integer.toString(size)).getBytes().length);		
			packet = new DatagramPacket(data, data.length, dstAddress);
			boolean sent = false;
			socket.send(packet);			//Sends the length
			try{
				socket.setSoTimeout(100);
				socket.receive(packet);
			}catch(java.net.SocketTimeoutException e) {
				System.out.println("No acknowledgement Received.");
			}*/



		} catch(Exception e) {
			e.printStackTrace();
		}
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
	}

}
