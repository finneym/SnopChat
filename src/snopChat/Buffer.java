package snopChat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Buffer {
	/**constants*/
	private final int MAX_SEQ_NUM =126;
	/**member data*/
	private String fileName;
	private byte[] mBuffer;
	private int mID;
	private int mExpSeqNo;
	private int mCounter;
	private boolean mFin;
	private int mSize;
	private int mClientPort;
	private int mServerPort;
	private InetAddress mAddress;

	File file;
	FileOutputStream fout;

	/**
	 * Constructor
	 * 
	 * creates an instance of buffer attached to a specific node
	 * to store data sent from other nodes
	 */
	Buffer(int node, int ServerPort, InetAddress address, int clientID, String format){
		this.mServerPort=ServerPort;
		this.mAddress = address;
		mID =node;
		mExpSeqNo=0;
		mCounter=0;
		mFin=false;
		fileName = "output-Server-"+node+"-Client-"+clientID+"."+format+"";
		mSize=0;
	}

	/**
	 * @return the mAddress
	 */
	public InetAddress getmAddressInet(){
		return mAddress;
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
		return mID;
	}
	/**
	 * @return the mFin
	 */
	public boolean getFin(){
		return this.mFin;
	}
	/**
	 * @return the mID
	 */
	public int getID(){
		return this.mID;
	}
	/**
	 * @return mExpSeqNo
	 */
	public int getExpSeqNum(){
		return this.mExpSeqNo;
	}
	/**
	 * @return mSize;
	 */
	public int getSize(){
		return mSize;
	}
	/**
	 * @return the length of mBuffer
	 */
	public int getLength(){
		return mBuffer.length;
	}
	/**
	 * @return mCounter;
	 */
	public int getCounter(){
		return this.mCounter;
	}
	/**
	 * if passed in seqNo is equal to expected seqNo, @return true; otherwise @return false
	 */
	public boolean checkSeqNum(int seqNo){
		return seqNo==mExpSeqNo;
	}
	/**
	 * if passed in id is equal to mID, @return true; otherwise @return false
	 */
	public boolean checkID(int id){
		return id==mID;
	}
	/**
	 * function to increment the expected seqNo
	 */
	public void moveOnSeqNum(){
		if(mExpSeqNo == MAX_SEQ_NUM){ //if maximum sequence number is reached, loop around to 1
			mExpSeqNo = 0;
		}
		else{
			mExpSeqNo++;
		}
	}
	/**
	 * function to set mExpSeqNum
	 */
	public void setExpSeqNo(int seqNum){
		this.mExpSeqNo=seqNum;
	}
	/**
	 * function to create a new buffer
	 */
	public void createBuffer(int size){
		mBuffer = new byte[size];
		mSize=size;
	}
	/**
	 * function to increase the counter by a given value
	 */
	public void counterIncrease(int toIncreaseBy){
		this.mCounter+=toIncreaseBy;
	}
	/**
	 * function to copy a received packet into the buffer
	 */
	public void copyIn(DatagramPacket packet, byte[] data){
		int lengthCopy =(packet.getLength()-2);
		System.arraycopy(data, 2, mBuffer, mCounter, lengthCopy);
	}
	/**
	 * function to set and return the finishing status
	 */
	public boolean checkFin(){
		if(!(this.mCounter<mSize)){
			this.mFin=true;
		}
		return mFin;
	}
	/**
	 * function to delete a file if it exists
	 */
	public void deletefile(){
		if(file != null && file.exists()){
			file.delete();
		}
	}
	/**
	 * thread function to combine the data in mBuffer into a file and print it to the screen
	 */
	public Runnable fin(){
		try {
			file= new File(fileName);				// Create file and write buffer into file
			fout= new FileOutputStream(file);
			fout.write(mBuffer, 0, mBuffer.length);
			fout.flush();
			fout.close();
			DisplayImage imageDisplay = new DisplayImage(fileName); 
			imageDisplay.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}

