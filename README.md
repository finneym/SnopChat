SnopChat
========

Welcome!

Node-
	smartphone/computer
	uses both server/client
	
	Functions-
		Send Image
		Receive Image
		

Client
	receives image
	replies ACK
	uses Buffer class to form Files

	Deletion-	
		Client waits for 10 seconds
		Calls Buffer to delete image
		sends ACK

	Functions-
		Receive
		sendACK
		delete stuff
	
Server
	sends image to everyone
	receives ACKs from individuals clients
	counts ACKs
	receives deletion ACKs and counts

	Functions-
		send image
		receive ack
		deletion stuff
	

Buffer
	copy from stopAndWait	
	add in second boolean to assure it's deleted
	
	Functions-
		make File
		add to data
		delete file


















		delete File