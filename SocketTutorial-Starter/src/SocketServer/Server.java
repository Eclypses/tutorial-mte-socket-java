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
package SocketServer;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.*;

public class Server {
	private static final int GENERAL_EXCEPTION = 1;
	private static final int UNKNOW_HOST_EXCEPTION = 2;	

	/**
	 * Run Socket Program
	 */
	public void run() throws Exception {
		try {

			//
			// This Tutorial uses Sockets for communication.
			// It should be noted that the MTE can be used with any type of communication.
			// (SOCKETS are not required!)
			//

			System.out.println("Starting Java Socket Server");

			//
			// Buffered input parameter
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			//
			// Set default server port
			int port = 27015;

			//
			// Prompt for port
			System.out.println("Please enter port to use, press Enter to use default:" + port);
			String portInput = br.readLine();
			if (!portInput.equals(null) && !portInput.equals("")) {
				port = Integer.parseInt(portInput);
			}

			//
			// Set server socket
			ServerSocket serverSocket = new ServerSocket(port);
			System.out.println("Listening for a new Client connection...");

			Socket socket = serverSocket.accept();
			System.out.println("Socket Server is listening on " + socket.getInetAddress() + ": port " + port);
			System.out.println("Connected with Client.");

			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();

			//
			// Receive message loop - ends when client disconnects
			while (true) {
				System.out.println("Listening for new messages from Client.");

				//
				// Receiving from Client
				byte[] lenBytes = new byte[4];
				int numReceived = is.read(lenBytes, 0, 4);

				if (numReceived == -1) {
					//
					// Client has disconnected break out of message loop
					break;
				}

				//
				// Get length - ensure BIG Endian
				int len = java.nio.ByteBuffer.wrap(lenBytes).getInt();

				int amtReceived = 0;
				byte[] receivedBytes = new byte[len];
				while (amtReceived < len) {
					amtReceived += is.read(receivedBytes, amtReceived, len - amtReceived);
				}

				// show packet received --> optional
				// This is for demonstration purposes ONLY and should NOT be done normally
				String received = new String(receivedBytes, 0, len);
				System.out.println("The received packet: " + received);

				// show packet sending out --> optional
				// This is for demonstration purposes ONLY and should NOT be done normally
				System.out.println("The packet being sent: " + received);

				int toSendLen = receivedBytes.length;
				// Make sure the bytes are in big endian format
				ByteBuffer toSendLenBytes = ByteBuffer.allocate(4);
				toSendLenBytes.order(ByteOrder.BIG_ENDIAN);
				toSendLenBytes.putInt(toSendLen);

				//
				// Send response back to client
				os.write(toSendLenBytes.array());
				os.write(receivedBytes);
			}

			//
			// We are out of the message loop - close connection
			socket.close();
			serverSocket.close();

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
		Server srv = new Server();
		srv.run();
	}
}