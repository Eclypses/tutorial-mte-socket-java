

<img src="Eclypses.png" style="width:50%;margin-right:0;"/>

<div align="center" style="font-size:40pt; font-weight:900; font-family:arial; margin-top:300px; " >
Java Socket Tutorial</div>
<br>
<div align="center" style="font-size:28pt; font-family:arial; " >
MTE Implementation Tutorial (MTE Core, MKE, MTE Fixed Length)</div>
<br>
<div align="center" style="font-size:15pt; font-family:arial; " >
Using MTE version 3.1.x</div>





[Introduction](#introduction)

[Socket Tutorial Server and Client](#socket-tutorial-server-and-client)


<div style="page-break-after: always; break-after: page;"></div>

# Introduction

This tutorial is sending messages via a socket connection. This is only a sample, the MTE does NOT require the usage of sockets, you can use whatever communication protocol that is needed.

This tutorial demonstrates how to use Mte Core, Mte MKE and Mte Fixed Length. For this application, only one type can be used at a time; however, it is possible to implement any and all at the same time depending on needs.

This tutorial contains two main programs, a client and a server, which may be run on Windows and Linux. It is required that the server is up and running awaiting for a client connection first before the client can be started; The client program will error out if it starts without being able to connect to a server application. Note that any of the available languages can be used for any available platform as long as communication is possible. It is just recommended that a server program is started first and then a client program can be started.

The MTE Encoder and Decoder need several pieces of information to be the same in order to function properly. This includes entropy, nonce, and personalization. If this information must be shared, the entropy MUST be passed securely. One way to do this is with a Diffie-Hellman approach. Each side will then be able to create two shared secrets to use as entropy for each pair of Encoder/Decoder. The two personalization values will be created by the client and shared to the other side. The two nonce values will be created by the server and shared.

The SDK that you received from Eclypses may not include the MKE or MTE FLEN add-ons. If your SDK contains either the MKE or the Fixed Length add-ons, the name of the SDK will contain "-MKE" or "-FLEN". If these add-ons are not there and you need them please work with your sales associate. If there is no need, please just ignore the MKE and FLEN options.

Here is a short explanation of when to use each, but it is encouraged to either speak to a sales associate or read the dev guide if you have additional concerns or questions.

***MTE Core:*** This is the recommended version of the MTE to use. Unless payloads are large or sequencing is needed this is the recommended version of the MTE and the most secure.

***MTE MKE:*** This version of the MTE is recommended when payloads are very large, the MTE Core would, depending on the token byte size, be multiple times larger than the original payload. Because this uses the MTE technology on encryption keys and encrypts the payload, the payload is only enlarged minimally.

***MTE Fixed Length:*** This version of the MTE is very secure and is used when the resulting payload is desired to be the same size for every transmission. The Fixed Length add-on is mainly used when using the sequencing verifier with MTE. In order to skip dropped packets or handle asynchronous packets the sequencing verifier requires that all packets be a predictable size. If you do not wish to handle this with your application then the Fixed Length add-on is a great choice. This is ONLY an encoder change - the decoder that is used is the MTE Core decoder.

***IMPORTANT NOTE***
>If using the fixed length MTE (FLEN), all messages that are sent that are longer than the set fixed length will be trimmed by the MTE. The other side of the MTE will NOT contain the trimmed portion. Also messages that are shorter than the fixed length will be padded by the MTE so each message that is sent will ALWAYS be the same length. When shorter message are "decoded" on the other side the MTE takes off the extra padding when using strings and hands back the original shorter message, BUT if you use the raw interface the padding will be present as all zeros. Please see official MTE Documentation for more information.

In this tutorial, there is an MTE Encoder on the client that is paired with an MTE Decoder on the server. Likewise, there is an MTE Encoder on the server that is paired with an MTE Decoder on the client. Secured messages wil be sent to and from both sides. If a system only needs to secure messages one way, only one pair could be used.

**IMPORTANT**
>Please note the solution provided in this tutorial does NOT include the MTE library or supporting MTE library files. If you have NOT been provided an MTE library and supporting files, please contact Eclypses Inc. The solution will only work AFTER the MTE library and MTE library files have been incorporated.
  

# Socket Tutorial Server and Client

## MTE Directory and File Setup
<ol>
<li>
Navigate to the "tutorial-mte-socket-java" directory.
</li>
<li>
Copy the contents from the MTE SDK "lib" directory into the "tutorial-mte-socket-java" directory.
</li>
<li>
Copy the contents from the MTE SDK "src/java" directory into the "tutorial-mte-socket-java" directory.
</li>
</ol>

Note that this repository currently has only been tested with the Windows operating system and includes the Eclypses, Inc. ECDH dynamic library for Windows. Other operating systems will need an appropriate ECDH library accordingly. 


## Source Code Key Points

### MTE Setup

<ol>
<li>
Comment/uncomment various code sections to more easily handle the function calls for the MTE Core or the add-on configurations. In the files "Client.java" and "Server.java", there are two sections that will need to be considered
<ul>

<li>

```java
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
```
</li>
</ul>

</li>

<li>
In this application, the Eclypses Elliptic Curve Diffie-Hellman (ECDH) support package is used to create entropy public and private keys. The public keys are then shared between the client and server, and then shared secrets are created to use as matching entropy for the creation of the Encoders and Decoders. The nonces are also created using the randomization feature of the support package.

```java
// Create the private and public keys.
int res = this.ecdhManager.createKeyPair(this.publicKey);
if (res < 0) {
    throw new Exception(String.valueOf(res));
}
```
The Java ECDHP256 class will keep the private key to itself and not provide access to the calling application.
</li>
<li>
The public keys created by the client will be sent to the server, and vice versa, and will be received as <i>peer public keys</i>. Then the shared secret can be created on each side. These should match as long as the information has been created and shared correctly.

```java
// Create temp byte array.
byte[] temp = new byte[EcdhP256.SzSecretData];

// Create shared secret.
int res = this.ecdhManager.getSharedSecret(peerKey, temp);
if (res < 0) {
    throw new Exception(String.valueOf(EcdhP256.MemoryFail));
}

return temp;
```
These secrets will then be used to fufill the entropy needed for the Encoders and Decoders.
</li>
<li>
The client will create the personalization strings, in this case a guid-like structure using the UUID library.

```java
// Create personalization strings.
String clientEncoderPersonal = java.util.UUID.randomUUID().toString();
clientEncoderInfo.setPersonalization(clientEncoderPersonal.getBytes(Charset.forName("UTF-8")));

String clientDecoderPersonal = java.util.UUID.randomUUID().toString();
clientDecoderInfo.setPersonalization(clientDecoderPersonal.getBytes(Charset.forName("UTF-8")));
```
</li>
<li>
The two public keys and the two personalization strings will then be sent to the server. The client will wait for an acknowledgment.

```java
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
```
</li>
<li>
The server will wait for the two public keys and the two personalization strings from the client. Once all four pieces of information have been received, it will send an acknowledgment.

```java
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
	if (serverDecoderInfo.getPeerPublicKey().length == 0) {
	    recvCount++;
	}
	serverDecoderInfo.setPeerPublicKey(recvData.message);
	break;
    case '2':
	if (serverDecoderInfo.getPersonalization().length == 0) {
	    recvCount++;
	}
	serverDecoderInfo.setPersonalization(recvData.message);
	break;
    case '3':
	if (serverEncoderInfo.getPeerPublicKey().length == 0) {
	    recvCount++;
	}
	serverEncoderInfo.setPeerPublicKey(recvData.message);
	break;
    case '4':
	if (serverEncoderInfo.getPersonalization().length == 0) {
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
```
</li>
<li>
The server will create the nonces, using the platform supplied secure RNG.

```java
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
```
</li>
<li>
The two public keys and the two nonces will then be sent to the client. The server will wait for an acknowledgment. 
```java
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
```
</li>

<li>
The client will now wait for information from the server. This includes the two server public keys, and the two nonces. Once all pieces of information have been obtained, the client will send an acknowledgment back to the server.

```java
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
```

</li>
<li>
After the client and server have exchanged their information, the client and server can each create their respective Encoder and Decoder. This is where the personalization string and nonce will be added. Additionally, the entropy will be set by getting the shared secret from ECDH. This sample code showcases the client Encoder. There will be four of each of these that will be very similar. Ensure carefully that each function uses the appropriate client/server, and Encoder/Decoder variables and functions.

```java
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
```

</li>
</ol>

### Diagnostic Test
<ol>
<li>
The application will run a diagnostic test, where the client will encode the word "ping", then send the encoded message to the server. The server will decode the received message to confirm that the original message is "ping". Then the server will encode the word "ack" and send the encoded message to the client. The client then decodes the received message, and confirms that it decodes it to the word "ack". 
</li>
</ol>

### User Interaction
<ol>
<li>
The application will continously prompt the user for an input (until the user types "quit"). That input will be encoded with the client Encoder and sent to the server.

```java
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
```
</li>
<li>
The server will use its Decoder to decode the message.

```java
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
```

</li>
<li>
Then that message will be re-encoded with the server Encoder and sent to the client.The client Decoder will then decode that message, which then will be compared with the original user input.
</li>
</ol>

<div style="page-break-after: always; break-after: page;"></div>

# Contact Eclypses

<img src="Eclypses.png" style="width:8in;"/>

<p align="center" style="font-weight: bold; font-size: 20pt;">Email: <a href="mailto:info@eclypses.com">info@eclypses.com</a></p>
<p align="center" style="font-weight: bold; font-size: 20pt;">Web: <a href="https://www.eclypses.com">www.eclypses.com</a></p>
<p align="center" style="font-weight: bold; font-size: 20pt;">Chat with us: <a href="https://developers.eclypses.com/dashboard">Developer Portal</a></p>
<p style="font-size: 8pt; margin-bottom: 0; margin: 300px 24px 30px 24px; " >

<b>All trademarks of Eclypses Inc.</b> may not be used without Eclypses Inc.'s prior written consent. No license for any use thereof has been granted without express written consent. Any unauthorized use thereof may violate copyright laws, trademark laws, privacy and publicity laws and communications regulations and statutes. The names, images and likeness of the Eclypses logo, along with all representations thereof, are valuable intellectual property assets of Eclypses, Inc. Accordingly, no party or parties, without the prior written consent of Eclypses, Inc., (which may be withheld in Eclypses' sole discretion), use or permit the use of any of the Eclypses trademarked names or logos of Eclypses, Inc. for any purpose other than as part of the address for the Premises, or use or permit the use of, for any purpose whatsoever, any image or rendering of, or any design based on, the exterior appearance or profile of the Eclypses trademarks and or logo(s).
</p>