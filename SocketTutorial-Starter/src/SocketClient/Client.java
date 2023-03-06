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

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Client
 * Starts Java Socket Client
 *
 */
public class Client {

	private static final int GENERAL_EXCEPTION = 1;
	private static final int UNKNOW_HOST_EXCEPTION = 2;	

	/**
	 * Run
	 * 
	 * @throws Exception
	 */
	public void run() throws Exception {
		try {
			//
			// This Tutorial uses Sockets for communication.
			// It should be noted that the MTE can be used with any type of communication.
			// (SOCKETS are not required!)
			//

			System.out.println("Starting Java Socket Client");

			//
			// Buffered input.
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			//
			// Set default server ip address
			String ipAddress = "localhost";
			System.out.println("Please enter ip address of Server, press Enter to use default:" + ipAddress);
			String ipAddressInput = br.readLine();

			if (!ipAddressInput.equals(null) && !ipAddressInput.equals("")) {
				ipAddress = ipAddressInput;
			}

			System.out.println("Server is at " + ipAddress);

			//
			// Set default server port
			int port = 27015;

			//
			// prompt for port
			System.out.println("Please enter port to use, press Enter to use default:" + port);
			String portInput = br.readLine();

			if (!portInput.equals(null) && !portInput.equals("")) {
				port = Integer.parseInt(portInput);
			}

			System.out.println("Connecting to server on port " + port);	

			Socket socket = new Socket(ipAddress, port);

			System.out.println("Client connected to Server.");

			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();

			//
			// Loop to get messages until quit is text
			while (true) {
				System.out.println("Please enter text to send: (To end please type 'quit')");
				String sendToServer = br.readLine();

				//
				// Check if we need to quit
				if (sendToServer.equalsIgnoreCase("quit")) {
					//
					// Break out of message loop
					break;
				}				

				// show packet sending out --> optional
				System.out.println("The packet being sent: " + sendToServer);
				
				//
				// Use the length of the message as the prefix
				byte[] toSendBytes = sendToServer.getBytes();
				int toSendLen = toSendBytes.length;

				//
				// Make sure the bytes are in big endian format
				ByteBuffer toSendLenBytes = ByteBuffer.allocate(4);
				toSendLenBytes.order(ByteOrder.BIG_ENDIAN);

				//
				// Send length prefix to server
				toSendLenBytes.putInt(toSendLen);

				//
				// Send to Server
				os.write(toSendLenBytes.array());
				os.write(toSendBytes);
				
				// Receiving back from server
				byte[] lenBytes = new byte[4];
				is.read(lenBytes, 0, 4);

				//
				// Get length - ensure BIG Endian
				int len = java.nio.ByteBuffer.wrap(lenBytes).getInt();

				int amtReceived = 0;
				byte[] receivedBytes = new byte[len];
				while (amtReceived < len) {
					amtReceived += is.read(receivedBytes, amtReceived, len - amtReceived);
				}

				//
				// show packet received --> optional
				// This is for demonstration purposes ONLY and should NOT be done normally
				String received = new String(receivedBytes, 0, len);
				System.out.println("The received packet: " + received + "\n");
			}

			//
			// when we break out of the message loop close the socket
			socket.close();

			System.out.println("Program stopped.");
			System.exit(0);

		} catch (UnknownHostException ex) {
			ex.printStackTrace();
			System.exit(UNKNOW_HOST_EXCEPTION);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(GENERAL_EXCEPTION);
		}
	}	

	/**
	 * Main
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Client client = new Client();
		client.run();
	}
}