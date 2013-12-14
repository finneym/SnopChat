package snopChat;

public class Details {
	private int mClientPort;
	private int mServerPort;
	private int mNodeId;
	
	Details(int node, int clientPort, int ServerPort){
		this.mClientPort=clientPort;
		this.mServerPort=ServerPort;
		this.mNodeId=node;
	}

	/**
	 * @return the mClientPort
	 */
	public int getClientPort() {
		return mClientPort;
	}

	/**
	 * @return the mServerPort
	 */
	public int getServerPort() {
		return mServerPort;
	}

	/**
	 * @return the mNodeId
	 */
	public int getNodeId() {
		return mNodeId;
	}


}
