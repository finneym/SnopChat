//package snopChat;
//
//import java.net.InetAddress;
//import java.net.UnknownHostException;
//
//public class Details {
//	private int mClientPort;
//	private int mServerPort;
//	private int mNodeId;
//	private String mAddress;
//	Details(int node, int clientPort, int ServerPort, String address){
//		this.mClientPort=clientPort;
//		this.mServerPort=ServerPort;
//		this.mNodeId=node;
//		this.mAddress = address;
//	}
//
//	public InetAddress getmAddressInet(){
//		try {
//			return InetAddress.getByName(mAddress);
//		} catch (UnknownHostException e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
//	public String getmAddress() {
//		return mAddress;
//	}
//
//	/**
//	 * @return the mClientPort
//	 */
//	public int getClientPort() {
//		return mClientPort;
//	}
//
//	/**
//	 * @return the mServerPort
//	 */
//	public int getServerPort() {
//		return mServerPort;
//	}
//
//	/**
//	 * @return the mNodeId
//	 */
//	public int getNodeId() {
//		return mNodeId;
//	}
//
//
//}
