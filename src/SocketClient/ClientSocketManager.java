/*
THIS SOFTWARE MAY NOT BE USED FOR PRODUCTION. Otherwise,
The MIT License (MIT)

Copyright (c) Eclypses, Inc.

All rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package SocketClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import com.tutorial.RecvMsg;

public class ClientSocketManager {
    private InputStream inputStream;
    private OutputStream outputStream;
    private Socket socket;

    public ClientSocketManager(String ipAddress, int port) throws IOException {
	// Create socket connection.
	socket = new Socket(ipAddress, port);

	System.out.println("Client connected to Server.");

	inputStream = socket.getInputStream();
	outputStream = socket.getOutputStream();
    }

    public void closeSocket() throws IOException {
	socket.close();
    }

    public int sendMessage(char header, byte[] message) throws IOException {
	// Data to send is in variable 'data' which is a byte array
	ByteBuffer msgHeader = ByteBuffer.allocate(5);
	// The default byte order of a ByteBuffer in Java is always
	// BIG Endian, which is what we want - but here we go anyway!
	msgHeader.order(ByteOrder.BIG_ENDIAN);
	msgHeader.putInt(message.length);
	msgHeader.put((byte) header);
	try {
	    outputStream.write(msgHeader.array());
	    outputStream.write(message);
	} catch (IOException e) {

	    System.err.println("Socket server is closed due to sending error.");
	    return 0;
	}

	return message.length;
    }

    public RecvMsg receiveMessage() throws IOException {
	// Create RecvMsg object.
	RecvMsg msgObj = new RecvMsg();

	// Create a 4-byte byte array for the length
	byte[] dataLengthRx = new byte[4];
	int i = 0;
	int rd;
	// Read until we have those 4 bytes
	try {
	    while (i < 4) {
		rd = inputStream.read(dataLengthRx, i, 4 - i);
		if (rd < 0) {
		    // error
		    return msgObj;
		}

		i += rd;
	    }
	} catch (IOException e) {
	    // error
	    return msgObj;
	}

	// ---------------------------------------------------------------
	// The default byte order of a ByteBuffer is always BIG Endian!
	// And that is what we want anyway. So we spare us the extra work
	// of setting up a named ByteBuffer instantiation and setting
	// up the byte order to ByteOrder.BIG_ENDIAN;
	// ---------------------------------------------------------------

	// Extract the length from ByteBuffer and add '1'
	int len = ByteBuffer.wrap(dataLengthRx).getInt() + 1;
	byte[] dataBytes = new byte[len];
	i = 0;
	// Read header byte and message in one loop
	try {
	    while (i < len) {
		rd = inputStream.read(dataBytes, i, len - i);
		if (rd < 0) {
		    // error
		    return msgObj;
		}
		i += rd;
	    }
	
	} catch (IOException e) {
	    // error
	    return msgObj;
	}
	// dataBytes now contains the header byte and the actual
	// message and this is how I pass it to the app's upper layer
	// where it is inspected and decoded if necessary
	
	// Get header byte from first byte.
	msgObj.header = (char)dataBytes[0];
	
	// The rest of the data is the actual message.
	msgObj.message = Arrays.copyOfRange(dataBytes, 1, len);
	msgObj.success = true;

	return msgObj;
    }

}
