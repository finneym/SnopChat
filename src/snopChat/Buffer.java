package snopChat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

public class Buffer {
	//static final String FILENAME = "output.jpg";
	private final int MAX_SEQ_NUM =15;
	private String fileName;
	private byte[] mBuffer;
	private int mID;
	File file;
	FileOutputStream fout;
	private int mExpSeqNo;
	private int mCounter;
	private boolean mFin;
	private int mSize;

	Buffer( int id){
		mID =id;
		mExpSeqNo=1;
		mCounter=0;
		mFin=false;
		fileName = "output"+mID+".jpg";
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
		int lengthCopy =(packet.getLength()-1);
		System.arraycopy(data, 1, mBuffer, mCounter, lengthCopy);
	}
	public void checkFin(){
		if(!(this.mCounter<mSize)){
			this.mFin=true;
			fin();
		}
	}

	private void fin(){
		try {
			file= new File(fileName);				// Create file and write buffer into file
			fout= new FileOutputStream(file);
			fout.write(mBuffer, 0, mBuffer.length);
			fout.flush();
			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

