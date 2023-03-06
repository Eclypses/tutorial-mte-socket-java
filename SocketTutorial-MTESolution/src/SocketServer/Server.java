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
import java.util.Base64;
import java.io.*;

/* Step 2 */
import com.eclypses.mte.*;

public class Server {
	private static final int GENERAL_EXCEPTION = 1;
	private static final int UNKNOW_HOST_EXCEPTION = 2;

	/* Step 3 */
	// --------------------------------------------
	// The fixed length, only needed for MTE FLEN
	// --------------------------------------------
	private static final int _fixedLength = 8;

	// ---------------------------------------------------
	// MKE and Fixed length add-ons are NOT in all SDK
	// MTE versions. If the name of the SDK includes
	// "-MKE" then it will contain the MKE add-on. If the
	// name of the SDK includes "-FLEN" then it contains
	// the Fixed length add-on.
	// ---------------------------------------------------
	// Create the Mte encoder, uncomment to use MTE core
	// ---------------------------------------------------
	private static MteEnc _encoder = new MteEnc();
	private static String _mteType = "Core";
	// ---------------------------------------------------
	// Create the Mte MKE encoder, uncomment to use MKE
	// ---------------------------------------------------
	// private static MteMkeEnc _encoder = new MteMkeEnc();
	// private static String _mteType = "MKE";
	// ---------------------------------------------------
	// Create the Mte Fixed length encoder, uncomment to use MTE FLEN
	// ---------------------------------------------------
	// private static MteFlenEnc _encoder = new MteFlenEnc(_fixedLength);
	// private static String _mteType = "FLEN";

	private static MteStatus _encoderStatus;

	/* Step 3 continue... */
	// ---------------------------------------------------
	// MKE and Fixed length add-ons are NOT in all SDK
	// MTE versions. If the name of the SDK includes
	// "-MKE" then it will contain the MKE add-on. If the
	// name of the SDK includes "-FLEN" then it contains
	// the Fixed length add-on.
	// ---------------------------------------------------
	// Create the MTE decoder, uncomment to use MTE core OR
	// Create the Mte Fixed length decoder (SAME as MTE Core), uncomment to use MTE
	// FLEN
	// ---------------------------------------------------
	private static MteDec _decoder = new MteDec();
	// ---------------------------------------------------
	// Create the Mte MKE decoder, uncomment to use MKE
	// ---------------------------------------------------
	// private static MteMkeDec _decoder = new MteMkeDec();

	private static MteStatus _decoderStatus;

	/* Step 5 */
	private static final String LicenseCompanyName = "LicenseCompanyName";
	private static final String LicenseKey = "LicenseKey";

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
			// Display what version of the MTE we are using
			String mteVersion = MteBase.getVersion();
			System.out.println("Using MTE Version " + mteVersion + "-" + _mteType);

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

			/* Step 4 */
			// Set default entropy, nonce and identifier
			// Providing Entropy in this fashion is insecure. This is for demonstration
			// purposes only and should never be done in practice.
			String identifier = "demo";
			long encoderNonce = 0;
			// OPTIONAL -- incrementing decoder nonce so same strings don't look the same
			// decoder and encoder nonce can be the same
			long decoderNonce = 1;

			//
			// Step 5
			// Check MTE license
			// Initialize MTE license. If a license code is not required (e.g., trial
			// mode), this can be skipped.
			if (!MteBase.initLicense(LicenseCompanyName, LicenseKey)) {
				_encoderStatus = MteStatus.mte_status_license_error;
				System.out.println("There was an error attempting to initialize the MTE License.");
				System.exit(0);
			}

			//
			// Step 6
			// Create the encoder
			createEncoder(identifier, encoderNonce);

			//
			// Step 6
			// Create the decoder
			createDecoder(identifier, decoderNonce);

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
				String received = Base64.getEncoder().encodeToString(receivedBytes);
				System.out.println("Base64 encoded representation of the received packet: " + received);

				//
				// Step 7
				// Decode received bytes and check for non-error response
	            // When checking the status on decode use "StatusIsError"
	            // Only checking if status is success can be misleading, there may be a
	            // warning returned that the user can ignore 
	            // See MTE Documentation for more details
				MteBase.StrStatus decoded = _decoder.decodeStr(receivedBytes);
				if (MteBase.statusIsError(decoded.status)) {
					System.err.println("Error decoding. Status: " + MteBase.getStatusName(decoded.status) + " / "
							+ MteBase.getStatusDescription(decoded.status));
					socket.close();
					serverSocket.close();
					System.exit(0);
				}
				System.out.println("Decoded data: " + decoded.str);

				//
				// Step 7
				// Encode returning text and ensure successful
				MteBase.ArrStatus encoded = _encoder.encode(decoded.str);
				if (encoded.status != MteStatus.mte_status_success) {
					System.err.println("Error encoding. Status: " + MteBase.getStatusName(encoded.status) + " / "
							+ MteBase.getStatusDescription(encoded.status));
					socket.close();
					serverSocket.close();
					System.exit(0);
				}

				// show packet sending out --> optional
				// This is for demonstration purposes ONLY and should NOT be done normally
				String sending = Base64.getEncoder().encodeToString(encoded.arr);
				System.out.println("Base64 encoded representation of the packet being sent: " + sending);

				int toSendLen = encoded.arr.length;
				// Make sure the bytes are in big endian format
				ByteBuffer toSendLenBytes = ByteBuffer.allocate(4);
				toSendLenBytes.order(ByteOrder.BIG_ENDIAN);
				toSendLenBytes.putInt(toSendLen);

				//
				// Send response back to client
				os.write(toSendLenBytes.array());
				os.write(encoded.arr);
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
	 * Creates the MTE Encoder
	 * 
	 * @param identifier
	 * @param nonce
	 * @throws Exception
	 */
	private void createEncoder(String identifier, long nonce) throws Exception {
		try {

			// Providing Entropy in this fashion is insecure. This is for demonstration
			// purposes only and should never be done in practice.
			// Entropy will be blank if using the Trial version of the MTE
			int entropyMinBytes = MteBase.getDrbgsEntropyMinBytes(_encoder.getDrbg());
			StringBuffer outputBuffer = new StringBuffer(entropyMinBytes);
			for (int i = 0; i < entropyMinBytes; i++) {
				outputBuffer.append("0");
			}
			String entropy = outputBuffer.toString();
			entropy.replace(' ', '0');

			//
			// Set encoder entropy and nonce
			_encoder.setEntropy(entropy.getBytes());
			_encoder.setNonce(nonce);

			//
			// Initialize MTE encoder
			_encoderStatus = _encoder.instantiate(identifier.getBytes());
			if (_encoderStatus != MteStatus.mte_status_success) {
				throw new Exception("Failed to initialize the MTE encoder engine. Status: "
						+ MteBase.getStatusName(_encoderStatus) + " / " + MteBase.getStatusDescription(_encoderStatus));
			}

		} catch (IOException e) {
			throw new Exception(e);
		}
	}

	/**
	 * Creates the MTE Decoder
	 * 
	 * @param identifier
	 * @param nonce
	 * @throws Exception
	 */
	private void createDecoder(String identifier, long nonce) throws Exception {
		try {

			// Providing Entropy in this fashion is insecure. This is for demonstration
			// purposes only and should never be done in practice.
			int entropyMinBytes = MteBase.getDrbgsEntropyMinBytes(_decoder.getDrbg());
			StringBuffer outputBuffer = new StringBuffer(entropyMinBytes);
			for (int i = 0; i < entropyMinBytes; i++) {
				outputBuffer.append("0");
			}
			String entropy = outputBuffer.toString();
			entropy.replace(' ', '0');

			//
			// Set decoder entropy and nonce
			_decoder.setEntropy(entropy.getBytes());
			_decoder.setNonce(nonce);

			//
			// Initialize MTE decoder
			_decoderStatus = _decoder.instantiate(identifier.getBytes());
			if (_encoderStatus != MteStatus.mte_status_success) {
				throw new Exception("Failed to initialize the MTE decoder engine. Status: "
						+ MteBase.getStatusName(_decoderStatus) + " / " + MteBase.getStatusDescription(_decoderStatus));
			}

		} catch (IOException e) {
			throw new Exception(e);
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