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


Jobs
	get it sending message

			-image
	
	intergrate flow control	
	
	getting hellos sending and receiving
		-updating arrayList()

	deletion stuff
	
	
	
	
	
we just discovered that we're able to use DatagramSockets for ACKs
Hellos can be formatted to be either

hello x.x.x.x port
frst 5 are hello
last (we need to dedicate a length of bytes to give to the port)
then turn the remainder of the byte to a String



	















		delete File