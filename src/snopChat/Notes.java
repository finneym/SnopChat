/*telecoms
each peer should be able to send and receive, yes sending fragments 
sends fragment of image to multicast address, how many acks should it wait for?
needs some sort of membership list at each node, recording how many peers are out there
each peer should periodically send out "hello" to the network to let others know it is there "peer 2"
once it gets all acks should send next fragment
should handel (ideally) two peers sending 
sender and receiver have to be able to work simultaniousely 
can only receive one packet at a time, have to have other code to handel what packet is
whenever it has an image it should send it out, should be able to have multiple senders sending images (sounds overcomplicated start with just one image sender then try increase)

*Added in tcdio
*/