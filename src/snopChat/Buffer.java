package snopChat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Buffer {
	//static final String FILENAME = "output.jpg";
	private final int MAX_SEQ_NUM =126;
	private String fileName;
	private byte[] mBuffer;
	private int mID;
	File file;
	FileOutputStream fout;
	private int mExpSeqNo;
	private int mCounter;
	private boolean mFin;
	private int mSize;
	private int mClientPort;
	private int mServerPort;
	//private int mNodeId;
	private InetAddress mAddress;
	private boolean fileDeleted = false;

	Buffer( int node, int ServerPort, InetAddress address, int clientID){
		this.mServerPort=ServerPort;
		this.mAddress = address;
		mID =node;
		mExpSeqNo=0;
		mCounter=0;
		mFin=false;
		fileName = "output-Server-"+node+"-Client-"+clientID+".jpg";
		mSize=0;

	}

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

	public boolean getFin(){
		return this.mFin;
	}
	public int getID(){
		return this.mID;
	}
	public boolean checkSeqNum(int seqNo){
		return seqNo==mExpSeqNo;
	}
	public boolean checkID(int id){
		return id==mID;
	}

	public void moveOnSeqNum(){
		if(mExpSeqNo == MAX_SEQ_NUM){
			mExpSeqNo = 0;
		}
		else{
			mExpSeqNo++;
		}
	}
	public void setExpSeqNo(int seqNum){
		this.mExpSeqNo=seqNum;
	}
	public int getExpSeqNum(){
		return this.mExpSeqNo;
	}
	public int getSize(){
		return mSize;
	}
	public void createBuffer(int size){
		mBuffer = new byte[size];
		mSize=size;
	}

	public int getLength(){
		return mBuffer.length;
	}

	public void counterIncrease(int toIncreaseBy){
		this.mCounter+=toIncreaseBy;
	}
	public int getCounter(){
		return this.mCounter;
	}

	public void copyIn(DatagramPacket packet, byte[] data){
		int lengthCopy =(packet.getLength()-2);
		System.arraycopy(data, 2, mBuffer, mCounter, lengthCopy);
	}
	public boolean checkFin(){
		if(!(this.mCounter<mSize)){
			this.mFin=true;
			new Thread(fin()).start();
			return true;
		}
		return false;
	}

	public boolean isDeleted(){
		return fileDeleted;
	}
	public void deletefile(){
		if(file != null && file.exists()){
			file.delete();
		}
	}
	private Runnable fin(){
		try {
			file= new File(fileName);				// Create file and write buffer into file
			fout= new FileOutputStream(file);
			fout.write(mBuffer, 0, mBuffer.length);
			fout.flush();
			fout.close();
			DisplayImage imageDisplay = new DisplayImage(fileName); 
			imageDisplay.start();

			try {
				Thread.sleep(10000);
				imageDisplay.off();
				if(file.delete()){
					this.fileDeleted = true;
				}
				return null;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}

