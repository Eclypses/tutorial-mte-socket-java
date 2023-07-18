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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.eclypses.mte.MteBase;
import com.eclypses.mte.MteBase.ArrStatus;
import com.eclypses.mte.MteDec;
import com.eclypses.mte.MteEnc;
import com.eclypses.mte.MteStatus;
import com.tutorial.MteSetupInfo;
import com.tutorial.RecvMsg;

/**
 * Client Starts Java Socket Client
 *
 */
public class Client {

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

    private static ClientSocketManager socketManager;
    private static MteSetupInfo clientEncoderInfo;
    private static MteSetupInfo clientDecoderInfo;

    /**
     * Run
     * 
     * @throws Exception
     */
    public void run() throws Exception {

	//
	// This Tutorial uses Sockets for communication.
	// It should be noted that the MTE can be used with any type of communication.
	// (SOCKETS are not required!)
	//

	System.out.println("Starting Java Socket Client");

	//
	// OPTIONAL Display what version of the MTE we are using
	String mteVersion = MteBase.getVersion();
	System.out.println("Using MTE Version " + mteVersion + "-" + _mteType);

	// Step 5
	// Check MTE license
	// Initialize MTE license. If a license code is not required (e.g., trial
	// mode), this can be skipped.
	if (!MteBase.initLicense(LicenseCompanyName, LicenseKey)) {
	    System.out.println("There was an error attempting to initialize the MTE License.");
	    return;
	}

	clientEncoderInfo = new MteSetupInfo();
	clientDecoderInfo = new MteSetupInfo();

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

	socketManager = new ClientSocketManager(ipAddress, port);

	// Exchange entropy, nonce, and personalization string between the client and
	// server.
	if (!exchangeMteInfo()) {
	    System.err.println("There was an error attempting to exchange information between this and the server.");
	    return;
	}

	// Create the Encoder.
	if (!createEncoder()) {
	    System.err.println("There was a problem creating the Encoder.");
	    return;
	}

	// Create the Decoder.
	if (!createDecoder()) {
	    System.err.println("There was a problem creating the Decoder.");
	    return;
	}

	// Run the diagnostic test.
	if (!runDiagnosticTest()) {
	    System.err.println("There was a problem creating the Decoder.");
	    return;
	}

	//
	// Loop to get messages until quit is text
	while (true) {
	    System.out.println("Please enter text to send: (To end please type 'quit')");
	    String input = br.readLine();

	    //
	    // Check if we need to quit
	    if (input.equalsIgnoreCase("quit")) {
		//
		// Break out of message loop
		break;
	    }

	    try {
		// Encode and send the input.
		if (!encodeAndSendMessage(input.getBytes(Charset.forName("UTF-8")))) {
		    break;
		}

		// Receive and decode the returned data.
		RecvMsg decoded = receiveAndDecodeMessage();
		if (!decoded.success) {
		    break;
		}

		// Compare the decoded message to the original.
		if (input.equals(new String(decoded.message, StandardCharsets.UTF_8))) {
		    System.out.println("The original input and decoded return match.");
		} else {
		    System.err.println("The original input and decoded return DO NOT match.");
		    break;
		}
	    } catch (Exception ex) {
		System.err.printf(ex.getMessage());
		System.exit(-1);
	    }
	}

	// Close client socket.
	socketManager.closeSocket();

	// Uninstantiate Encoder and Decoder.
	_encoder.uninstantiate();
	_decoder.uninstantiate();

	System.out.println("Program stopped.");
	System.exit(0);

    }

    /**
     * Creates the MTE Encoder
     * 
     * @throws Exception *
     */
    private boolean createEncoder() throws Exception {
	// Display all info related to the client Encoder.
	System.out.println("Client Encoder public key:");
	System.out.println(bytessToHex(clientEncoderInfo.getPublicKey()));
	System.out.println("Client Encoder peer's key:");
	System.out.println(bytessToHex(clientEncoderInfo.getPeerPublicKey()));
	System.out.println("Client Encoder nonce:");
	System.out.println(bytessToHex(clientEncoderInfo.getNonce()));
	System.out.println("Client Encoder personalization:");
	System.out.println(new String(clientEncoderInfo.getPersonalization()));

	// Create shared secret.
	byte[] secret = clientEncoderInfo.getSharedSecret();

	// Set Encoder entropy using this shared secret.
	_encoder.setEntropy(secret);

	// Set Encoder nonce.
	_encoder.setNonce(clientEncoderInfo.getNonce());

	// Instantiate Encoder.
	MteStatus status = _encoder.instantiate(clientEncoderInfo.getPersonalization());
	if (status != MteStatus.mte_status_success) {
	    System.err.printf("Encoder instantiate error %s: %s", MteBase.getStatusName(status),
		    MteBase.getStatusDescription(status));
	    return false;
	}

	// Delete client Encoder info.
	clientEncoderInfo = null;

	return true;
    }

    /**
     * Creates the MTE Decoder
     * 
     * @throws Exception *
     */
    private boolean createDecoder() throws Exception {
	// Display all info related to the client Decoder.
	System.out.println("Client Decoder public key:");
	System.out.println(bytessToHex(clientDecoderInfo.getPublicKey()));
	System.out.println("Client Decoder peer's key:");
	System.out.println(bytessToHex(clientDecoderInfo.getPeerPublicKey()));
	System.out.println("Client Decoder nonce:");
	System.out.println(bytessToHex(clientDecoderInfo.getNonce()));
	System.out.println("Client Decoder personalization:");
	System.out.println(new String(clientDecoderInfo.getPersonalization()));

	// Create shared secret.
	byte[] secret = clientDecoderInfo.getSharedSecret();

	// Set Decoder entropy using this shared secret.
	_decoder.setEntropy(secret);

	// Set Decoder nonce.
	_decoder.setNonce(clientDecoderInfo.getNonce());

	// Instantiate Decoder.
	MteStatus status = _decoder.instantiate(clientDecoderInfo.getPersonalization());
	if (status != MteStatus.mte_status_success) {
	    System.err.printf("Decoder instantiate error %s: %s", MteBase.getStatusName(status),
		    MteBase.getStatusDescription(status));
	    return false;
	}

	// Delete client Decoder info.
	clientDecoderInfo = null;

	return true;
    }

    private boolean exchangeMteInfo() throws IOException {
	// The client Encoder and the server Decoder will be paired.
	// The client Decoder and the server Encoder will be paired.

	// Prepare to send client information.

	// Create personalization strings.
	String clientEncoderPersonal = java.util.UUID.randomUUID().toString();
	clientEncoderInfo.setPersonalization(clientEncoderPersonal.getBytes(Charset.forName("UTF-8")));

	String clientDecoderPersonal = java.util.UUID.randomUUID().toString();
	clientDecoderInfo.setPersonalization(clientDecoderPersonal.getBytes(Charset.forName("UTF-8")));

	// Send out information to the server.
	// 1 - client Encoder public key (to server Decoder)
	// 2 - client Encoder personalization string (to server Decoder)
	// 3 - client Decoder public key (to server Encoder)
	// 4 - client Decoder personalization string (to server Encoder)
	socketManager.sendMessage('1', clientEncoderInfo.getPublicKey());
	socketManager.sendMessage('2', clientEncoderInfo.getPersonalization());
	socketManager.sendMessage('3', clientDecoderInfo.getPublicKey());
	socketManager.sendMessage('4', clientDecoderInfo.getPersonalization());

	// Wait for ack from server.
	RecvMsg recvData = socketManager.receiveMessage();
	if (recvData.header != 'A') {
	    return false;
	}

	recvData = null;

	// Processing incoming message all 4 will be needed.
	int recvCount = 0;

	// Loop until all 4 data are received from server, can be in any order.
	while (recvCount < 4) {
	    // Receive the next message from the server.
	    recvData = socketManager.receiveMessage();

	    // Evaluate the header.
	    // 1 - client Decoder public key (from server Encoder)
	    // 2 - client Decoder nonce (from server Encoder)
	    // 3 - client Encoder public key (from server Decoder)
	    // 4 - client Encoder nonce (from server Decoder)
	    switch (recvData.header) {
	    case '1':
		if (isNullOrEmpty(clientDecoderInfo.getPeerPublicKey())) {
		    recvCount++;
		}
		clientDecoderInfo.setPeerPublicKey(recvData.message);
		break;
	    case '2':
		if (isNullOrEmpty(clientDecoderInfo.getNonce())) {
		    recvCount++;
		}
		clientDecoderInfo.setNonce(recvData.message);
		break;
	    case '3':
		if (isNullOrEmpty(clientEncoderInfo.getPeerPublicKey())) {
		    recvCount++;
		}
		clientEncoderInfo.setPeerPublicKey(recvData.message);
		break;
	    case '4':
		if (isNullOrEmpty(clientEncoderInfo.getNonce())) {
		    recvCount++;
		}
		clientEncoderInfo.setNonce(recvData.message);
		break;
	    default:
		// Unknown message, abort here, send an 'E' for error.
		socketManager.sendMessage('E', "ERR".getBytes(Charset.forName("UTF-8")));
		break;
	    }
	}

	// Now all values from server have been received, send an 'A' for acknowledge to
	// server.
	socketManager.sendMessage('A', "ACK".getBytes(Charset.forName("UTF-8")));

	return true;
    }

    private boolean runDiagnosticTest() throws IOException {
	// Create ping message.
	String message = "ping";

	// Encode and send message.
	if (!encodeAndSendMessage(message.getBytes(Charset.forName("UTF-8")))) {
	    return false;
	}

	// Receive and decode the message.
	RecvMsg decoded = receiveAndDecodeMessage();
	if (!decoded.success) {
	    return false;
	}

	// Check that it successfully decoded as "ack".
	if ("ack".equals(new String(decoded.message, StandardCharsets.UTF_8))) {
	    System.out.println("Client Decoder decoded the message from the server Encoder successfully.");
	} else {
	    System.out.println("Client Decoder DID NOT decode the message from the server Encoder successfully.");
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
	Client client = new Client();
	client.run();
    }
}