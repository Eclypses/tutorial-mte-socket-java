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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.eclypses.ecdh.EcdhP256;
import com.eclypses.mte.MteBase;
import com.eclypses.mte.MteBase.ArrStatus;
import com.eclypses.mte.MteDec;
import com.eclypses.mte.MteEnc;
import com.eclypses.mte.MteStatus;
import com.tutorial.MteSetupInfo;
import com.tutorial.RecvMsg;

public class Server {
    private static final int GENERAL_EXCEPTION = 1;
    private static final int UNKNOW_HOST_EXCEPTION = 2;

    /* Step 3 */
    // --------------------------------------------
    // The fixed length, only needed for MTE FLEN
    // --------------------------------------------
    // private static final int _fixedLength = 8;

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

    /* Step 3 continue... */
    // ---------------------------------------------------
    // MKE and Fixed length add-ons are NOT in all SDK
    // MTE versions. If the name of the SDK includes
    // "-MKE" then it will contain the MKE add-on. If the
    // name of the SDK includes "-FLEN" then it contains
    // the Fixed length add-on.
    // ---------------------------------------------------
    // Create the MTE decoder, uncomment to use MTE core OR
    // Create the Mte Fixed length decoder (SAME as MTE Core), uncomment to use
    // MTE
    // FLEN
    // ---------------------------------------------------
    private static MteDec _decoder = new MteDec();
    // ---------------------------------------------------
    // Create the Mte MKE decoder, uncomment to use MKE
    // ---------------------------------------------------
    // private static MteMkeDec _decoder = new MteMkeDec();

    /* Step 5 */
    private static final String LicenseCompanyName = "LicenseCompanyName";
    private static final String LicenseKey = "LicenseKey";

    private static ServerSocketManager socketManager;
    private static MteSetupInfo serverEncoderInfo;
    private static MteSetupInfo serverDecoderInfo;

    /**
     * Run Socket Program
     */
    public void run() throws Exception {
	try {

	    //
	    // This Tutorial uses Sockets for communication.
	    // It should be noted that the MTE can be used with any type of
	    // communication.
	    // (SOCKETS are not required!)
	    //

	    System.out.println("Starting Java Socket Server");

	    //
	    // Display what version of the MTE we are using
	    String mteVersion = MteBase.getVersion();
	    System.out.println("Using MTE Version " + mteVersion + "-" + _mteType);

	    serverEncoderInfo = new MteSetupInfo();
	    serverDecoderInfo = new MteSetupInfo();

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
	    // Step 5
	    // Check MTE license
	    // Initialize MTE license. If a license code is not required (e.g., trial
	    // mode), this can be skipped.
	    if (!MteBase.initLicense(LicenseCompanyName, LicenseKey)) {
		System.out.println("There was an error attempting to initialize the MTE License.");
		System.exit(0);
	    }

	    socketManager = new ServerSocketManager(port);

	    // Exchange entropy, nonce, and personalization strings between the client
	    // and
	    // server.
	    if (!exchangeMteInfo()) {
		System.out
			.println("There was an error attempting to exchange information between this and the client.");
		return;
	    }

	    //
	    // Step 6
	    // Create the Encoder
	    if (!createEncoder()) {
		System.out.println("There was an error attempting to create the Encoder.");
		return;
	    }

	    //
	    // Step 6
	    // Create the Decoder
	    if (!createDecoder()) {
		System.out.println("There was an error attempting to create the Decoder.");
		return;
	    }

	    // Run the diagnostic test.
	    if (!runDiagnosticTest()) {
		System.out.println("There was a problem running the diagnostic test.");
		return;
	    }

	    //
	    // Receive message loop - ends when client disconnects
	    while (true) {
		System.out.println("Listening for messages from client...");

		// Receive and decode the message from the client.
		RecvMsg decoded = receiveAndDecodeMessage();
		if (decoded.success == false) {
		    break;
		}

		// Encode and send the input.
		if (encodeAndSendMessage(decoded.message) == false) {
		    break;
		}

		// Free the decoded message.
		decoded = null;

	    }

	    // Close server socket.
	    socketManager.closeSocket();

	    // Uninstantiate Encoder and Decoder.
	    _encoder.uninstantiate();
	    _decoder.uninstantiate();

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
     * @throws Exception *
     */
    private boolean createEncoder() throws Exception {
	// Display all info related to the server Encoder.
	System.out.println("Server Encoder public key:");
	System.out.println(bytessToHex(serverEncoderInfo.getPublicKey()));
	System.out.println("Server Encoder peer's key:");
	System.out.println(bytessToHex(serverEncoderInfo.getPeerPublicKey()));
	System.out.println("Server Encoder nonce:");
	System.out.println(bytessToHex(serverEncoderInfo.getNonce()));
	System.out.println("Server Encoder personalization:");
	System.out.println(new String(serverEncoderInfo.getPersonalization()));

	// Create shared secret.
	byte[] secret = serverEncoderInfo.getSharedSecret();

	// Set Encoder entropy using this shared secret.
	_encoder.setEntropy(secret);

	// Set Encoder nonce.
	_encoder.setNonce(serverEncoderInfo.getNonce());

	// Instantiate Encoder.
	MteStatus status = _encoder.instantiate(serverEncoderInfo.getPersonalization());
	if (status != MteStatus.mte_status_success) {
	    System.err.printf("Encoder instantiate error %s: %s", MteBase.getStatusName(status),
		    MteBase.getStatusDescription(status));
	    return false;
	}

	// Delete server Encoder info.
	serverEncoderInfo = null;

	return true;
    }

    /**
     * Creates the MTE Decoder
     * 
     * @throws Exception *
     */
    private boolean createDecoder() throws Exception {
	// Display all info related to the server Decoder.
	System.out.println("Server Decoder public key:");
	System.out.println(bytessToHex(serverDecoderInfo.getPublicKey()));
	System.out.println("Server Decoder peer's key:");
	System.out.println(bytessToHex(serverDecoderInfo.getPeerPublicKey()));
	System.out.println("Server Decoder nonce:");
	System.out.println(bytessToHex(serverDecoderInfo.getNonce()));
	System.out.println("Server Decoder personalization:");
	System.out.println(new String(serverDecoderInfo.getPersonalization()));

	// Create shared secret.
	byte[] secret = serverDecoderInfo.getSharedSecret();

	// Set Decoder entropy using this shared secret.
	_decoder.setEntropy(secret);

	// Set Decoder nonce.
	_decoder.setNonce(serverDecoderInfo.getNonce());

	// Instantiate Decoder.
	MteStatus status = _decoder.instantiate(serverDecoderInfo.getPersonalization());
	if (status != MteStatus.mte_status_success) {
	    System.err.printf("Decoder instantiate error %s: %s", MteBase.getStatusName(status),
		    MteBase.getStatusDescription(status));
	    return false;
	}

	// Delete server Decoder info.
	serverDecoderInfo = null;

	return true;
    }

    private boolean exchangeMteInfo() throws IOException {
	// The client Encoder and the server Decoder will be paired.
	// The client Decoder and the server Encoder will be paired.

	// Processing incoming message all 4 will be needed.
	int recvCount = 0;
	RecvMsg recvData = new RecvMsg();

	// Loop until all 4 data are received from client, can be in any order.
	while (recvCount < 4) {
	    // Receive the next message from the client.
	    recvData = socketManager.receiveMessage();

	    // Evaluate the header.
	    // 1 - server Decoder public key (from client Encoder)
	    // 2 - server Decoder personalization string (from client Encoder)
	    // 3 - server Encoder public key (from client Decoder)
	    // 4 - server Encoder personalization string (from client Decoder)
	    switch (recvData.header) {
	    case '1':
		if (isNullOrEmpty(serverDecoderInfo.getPeerPublicKey())) {
		    recvCount++;
		}
		serverDecoderInfo.setPeerPublicKey(recvData.message);
		break;
	    case '2':
		if (isNullOrEmpty(serverDecoderInfo.getPersonalization())) {
		    recvCount++;
		}
		serverDecoderInfo.setPersonalization(recvData.message);
		break;
	    case '3':
		if (isNullOrEmpty(serverEncoderInfo.getPeerPublicKey())) {
		    recvCount++;
		}
		serverEncoderInfo.setPeerPublicKey(recvData.message);
		break;
	    case '4':
		if (isNullOrEmpty(serverEncoderInfo.getPersonalization())) {
		    recvCount++;
		}
		serverEncoderInfo.setPersonalization(recvData.message);
		break;
	    default:
		// Unknown message, abort here, send an 'E' for error.
		socketManager.sendMessage('E', "ERR".getBytes(Charset.forName("UTF-8")));
		break;
	    }
	}

	// Now all values from client have been received, send an 'A' for
	// acknowledge to
	// client.
	socketManager.sendMessage('A', "ACK".getBytes(Charset.forName("UTF-8")));

	// Prepare to send server information now.

	// Create nonces.
	int minNonceBytes = MteBase.getDrbgsNonceMinBytes(_encoder.getDrbg());
	if (minNonceBytes <= 0) {
	    minNonceBytes = 1;
	}

	byte[] serverEncoderNonce = new byte[minNonceBytes];
	int res = EcdhP256.getRandom(serverEncoderNonce);
	if (res < 0) {
	    return false;
	}
	serverEncoderInfo.setNonce(serverEncoderNonce);

	byte[] serverDecoderNonce = new byte[minNonceBytes];
	res = EcdhP256.getRandom(serverDecoderNonce);
	if (res < 0) {
	    return false;
	}
	serverDecoderInfo.setNonce(serverDecoderNonce);

	// Send out information to the client.
	// 1 - server Encoder public key (to client Decoder)
	// 2 - server Encoder nonce (to client Decoder)
	// 3 - server Decoder public key (to client Encoder)
	// 4 - server Decoder nonce (to client Encoder)
	socketManager.sendMessage('1', serverEncoderInfo.getPublicKey());
	socketManager.sendMessage('2', serverEncoderInfo.getNonce());
	socketManager.sendMessage('3', serverDecoderInfo.getPublicKey());
	socketManager.sendMessage('4', serverDecoderInfo.getNonce());

	// Wait for ack from client.
	recvData = socketManager.receiveMessage();
	if (recvData.header != 'A') {
	    return false;
	}

	return true;
    }

    private boolean runDiagnosticTest() throws IOException {
	// Receive and decode the message.
	RecvMsg decoded = receiveAndDecodeMessage();
	if (!decoded.success) {
	    return false;
	}

	// Check that it successfully decoded as "ping".
	if ("ping".equals(new String(decoded.message, StandardCharsets.UTF_8))) {
	    System.out.println("Server Decoder decoded the message from the client Encoder successfully.");
	} else {
	    System.out.println("Server Decoder DID NOT decode the message from the client Encoder successfully.");
	    return false;
	}

	// Create "ack" message.
	String message = "ack";

	// Encode and send message.
	if (!encodeAndSendMessage(message.getBytes(Charset.forName("UTF-8")))) {
	    return false;
	}

	return true;
    }

    private boolean encodeAndSendMessage(byte[] message) throws IOException {

	// Display original message.
	System.out.printf("Message to be encoded: %s\n", new String(message, StandardCharsets.UTF_8));

	ArrStatus encoded = _encoder.encode(message);
	if (encoded.status != MteStatus.mte_status_success) {
	    System.err.printf("Error encoding (%s): %s", MteBase.getStatusName(encoded.status),
		    MteBase.getStatusDescription(encoded.status));
	    return false;
	}

	// Send the encoded message.
	int res = socketManager.sendMessage('m', encoded.arr);
	if (res <= 0) {
	    return false;
	}

	// Display encoded message.
	System.out.printf("Encoded message being sent: %s\n", bytessToHex(encoded.arr));

	return true;
    }

    private RecvMsg receiveAndDecodeMessage() throws IOException {
	// Wait for return message.
	RecvMsg msgObj = socketManager.receiveMessage();

	if (msgObj.success == false || msgObj.message.length == 0) {
	    return msgObj;
	}

	// Display encoded message.
	System.out.printf("Encoded message received: %s\n", bytessToHex(msgObj.message));

	// Decode the message.
	ArrStatus decoded = _decoder.decode(msgObj.message);
	if (MteBase.statusIsError(decoded.status)) {
	    System.err.printf("Error decoding (%s): %s", MteBase.getStatusName(decoded.status),
		    MteBase.getStatusDescription(decoded.status));
	    return msgObj;
	}

	msgObj.success = true;
	msgObj.message = decoded.arr;

	// Display decoded message.
	System.out.printf("Decoded message: %s\n", new String(msgObj.message, StandardCharsets.UTF_8));

	return msgObj;
    }

    private String bytessToHex(byte[] data) {
	String hex = "";

	// Go through each byte to format to hex.
	for (byte i : data) {
	    hex += String.format("%02X", i);
	}
	return hex;
    }
    
    private boolean isNullOrEmpty(byte[] data) {
  	// Check if the byte array is null.
  	if (data == null) {
  	return true;
  	}
  	
  	if (data.length == 0) {
  	    return true;
  	}	
  	
  	return false;
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