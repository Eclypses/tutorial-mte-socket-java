

<img src="Eclypses.png" style="width:50%;margin-right:0;"/>

<div align="center" style="font-size:40pt; font-weight:900; font-family:arial; margin-top:300px; " >
Linux Java Socket Tutorial</div>

<div align="center" style="font-size:28pt; font-family:arial; " >
MTE<sup>TM</sup> Implementation Tutorial </div>
<div align="center" style="font-size:15pt; font-family:arial; " >
Using MTE<sup>TM</sup> version 2.1</div>





[Introduction](#introduction)

[Socket Tutorial Server and Client](#socket-tutorial-server-and-client)


<div style="page-break-after: always; break-after: page;"></div>

# Introduction

This tutorial is sending messages via a socket connection. This is only a sample, the MTE does NOT require the usage of sockets, you can use whatever communication protocol that is needed.

This tutorial demonstrates how to use Mte Core, Mte MKE and Mte Fixed Length. Depending on what your needs are, these three different implementations can be used in the same application OR you can use any one of them. They are not dependent on each other and can run simultaneously in the same application if needed. 

The SDK that you received from Eclypses may not include the MKE or MTE FLEN add-ons. If your SDK contains either the MKE or the Fixed Length add-ons, the name of the SDK will contain "-MKE" or "-FLEN". If these add-ons are not there and you need them please work with your sales associate. If there is no need, please just ignore the MKE and FLEN options.

Here is a short explanation of when to use each, but it is encouraged to either speak to a sales associate or read the dev guide if you have additional concerns or questions.

***MTE Core:*** This is the recommended version of the MTE to use. Unless payloads are large or sequencing is needed this is the recommended version of the MTE and the most secure.

***MTE MKE:*** This version of the MTE is recommended when payloads are very large, the MTE Core would, depending on the token byte size, be multiple times larger than the original payload. Because this uses the MTE technology on encryption keys and encrypts the payload, the payload is only enlarged minimally.

***MTE Fixed Length:*** This version of the MTE is very secure and is used when the resulting payload is desired to be the same size for every transmission. The Fixed Length add-on is mainly used when using the sequencing verifier with MTE. In order to skip dropped packets or handle asynchronous packets the sequencing verifier requires that all packets be a predictable size. If you do not wish to handle this with your application then the Fixed Length add-on is a great choice. This is ONLY an encoder change - the decoder that is used is the MTE Core decoder.

In this tutorial we are creating an MTE Encoder and an MTE Decoder in the server as well as the client because we are sending secured messages in both directions. This is only needed when there are secured messages being sent from both sides, the server as well as the client. If only one side of your application is sending secured messages, then the side that sends the secured messages should have an Encoder and the side receiving the messages needs only a Decoder.

These steps should be followed on the server side as well as on the client side of the program.

**IMPORTANT**
>Please note the solution provided in this tutorial does NOT include the MTE library or supporting MTE library files. If you have NOT been provided a MTE library and supporting files, please contact Eclypses Inc. The solution will only work AFTER MTE library and MTE library files have been included.


# Socket Tutorial Server and Client

<ol>
<li>Add the "com" folder in the mte-Linux package inside the src/Java folder (should be “com” folder) to the SocketServer and the SocketClient folder. Also copy the libmtejni.so file from the "lib" folder in the mte-Linux package to the /lib folder on the linux distribution. (Or once of the default folders linux looks for the library files on the distribution.</li>
<br>

```sh
sudo cp /path/to/libmtejni.so /lib
```

<br>
<li>Add a “import com.eclypses.mte.*;” to the top of the Server.java and the Client.java files.</li>
<br>
<li>Create the MTE Decoder and MTE Encoder as well as the accompanying MTE<sup>TM</sup> status for each as global variables. Also include fixed length parameter if using FLEN.</li>

***IMPORTANT NOTE***
> If using the fixed length MTE (FLEN), all messages that are sent that are longer than the set fixed length will be trimmed by the MTE. The other side of the MTE will NOT contain the trimmed portion. Also messages that are shorter than the fixed length will be padded by the MTE so each message that is sent will ALWAYS be the same length. When shorter message are "decoded" on the other side the MTE takes off the extra padding when using strings and hands back the original shorter message, BUT if you use the raw interface the padding will be present as all zeros. Please see official MTE Documentation for more information.

```java
//--------------------------------------------
// The fixed length, only needed for MTE FLEN
//--------------------------------------------
private static final int _fixedLength = 8;

//---------------------------------------------------
// MKE and FLEN add-ons are NOT in all
// SDK MTE versions, the name of the SDK will contain
// "-MKE" or "-FLEN" if it has these add-ons.
//---------------------------------------------------
// Create the Mte encoder, uncomment to use MTE core
//---------------------------------------------------
private static MteEnc _encoder = new MteEnc();
//---------------------------------------------------
// Create the Mte MKE encoder, uncomment to use MKE
//---------------------------------------------------
// private static MteMkeEnc _encoder = new MteMkeEnc();
//---------------------------------------------------
// Create the Mte Fixed length encoder, uncomment to use MTE FLEN
//---------------------------------------------------
// private static MteFlenEnc _encoder = new MteFlenEnc(_fixedLength);
// private static MteStatus _encoderStatus;

/* Step 3 continue... */
//---------------------------------------------------
// MKE and FLEN add-ons are NOT in all
// SDK MTE versions, the name of the SDK will contain
// "-MKE" or "-FLEN" if it has these add-ons.
//---------------------------------------------------
// Create the MTE decoder, uncomment to use MTE core OR FLEN
// Create the Mte Fixed length decoder (SAME as MTE Core)
//---------------------------------------------------
private static MteDec _decoder = new MteDec();
//---------------------------------------------------
// Create the Mte MKE decoder, uncomment to use MKE
//---------------------------------------------------
// private static MteMkeDec _decoder = new MteMkeDec();
private static MteStatus _decoderStatus;

```

<li>Set the default entropy, nonce and personalization value.</li>
These values should be treated like encryption keys. For demonstration purposes in the tutorial we are simply prompting the user for values or allowing default values to be set. In a production environment these values should be protected and not available to outside sources. 

For the entropy, we have to determine the size of the allowed entropy value based on the drbg we have selected. Because we are using the MTE default DRBG we will determine this as we are creating the decoder and encoder.

<li>To ensure the MTE library is licensed correctly run the license check and also run the DRBGS self test. The LicenseCompanyName, and LicenseKey below should be replaced with your company’s MTE license information. If a trial version of MTE is being used any value can be passed into those fields and it will work.</li>

```java
//
// Check mte license
// Initialize MTE license
if (!MteBase.initLicense(“LicenseCompanyName”, “LicenseKey”))
{
    _encoderStatus = MteStatus.mte_status_license_error;
    System.out.print("License error ({0}): {1}. Press ENTER to end.",
                    MteBase.getStatusName(_encoderStatus),
                    MteBase.getStatusDescription(_encoderStatus));
    String end = br.readLine();
	System.exit(0);
 }

<li>Create MTE Decoder Instance and MTE Encoder Instances in a couple functions.</li>

Here is a sample function that creates the MTE Decoder.

```java
private void createDecoder(String identifier, long nonce) throws Exception
{
    try {
		
		//
		// Providing Entropy in this fashion is insecure. This is for demonstration 
		// purposes only and should never be done in practice.
		int entropyMinBytes = MteBase.getDrbgsEntropyMinBytes(_decoder.getDrbg());
		StringBuffer outputBuffer = new StringBuffer(entropyMinBytes);
		for (int i = 0; i < entropyMinBytes; i++){
		   outputBuffer.append("0");
		}
		String entropy = outputBuffer.toString();
		entropy.replace(' ', '0');
		
		//
		// Set decoder entropy and nonce 
		_decoder.setEntropy(entropy.getBytes());
		_decoder.setNonce(nonce);
		
		//
        // Initialize MTE encoder
		_decoderStatus = _decoder.instantiate(identifier.getBytes());
		if (_decoderStatus != MteStatus.mte_status_success)
        {
            throw new Exception("Failed to initialize the MTE decoder engine. Status: " +MteBase.getStatusName(_decoderStatus)+ " / " + MteBase.getStatusDescription(_decoderStatus));
        }
		
	} catch(IOException e){
		throw new Exception(e);
	}
}

```
*(For further info on Decoder constructor – DevelopersGuide)*

Here is a sample function that creates the MTE Encoder.

```java
private void createEncoder(String entropy, String identifier, long nonce) throws Exception
{
    try {
		  
		  //
		  // Providing Entropy in this fashion is insecure. This is for demonstration 
		  // purposes only and should never be done in practice.
		  int entropyMinBytes = MteBase.getDrbgsEntropyMinBytes(_encoder.getDrbg());
		  StringBuffer outputBuffer = new StringBuffer(entropyMinBytes);
		  for (int i = 0; i < entropyMinBytes; i++){
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
          if (_encoderStatus != MteStatus.mte_status_success)
          {
              throw new Exception("Failed to initialize the MTE encoder engine. Status: " +MteBase.getStatusName(_encoderStatus)+ " / " + MteBase.getStatusDescription(_encoderStatus));
          }
		  
	} catch(IOException e){
	    throw new Exception(e);
	}
}

```
*(For further info on Encode constructor – DevelopersGuide)*

Instantiate the MTE Decoder and MTE Encoder by calling that function at the start of your main function:

```java

// Create the encoder
createEncoder(entropy, identifier, encoderNonce);

// Create the decoder
createDecoder(entropy, identifier, decoderNonce);
```

<li>Finally, we need to add the MTE calls to encode and decode the messages that we are sending and receiving from the other side. (Ensure on the server side the Encoder is called to encode the outgoing text, then the Decoder is called to decode the incoming response.)</li>

<br>
Here is a sample of how to call the encoder and decoder.

```java
// 
// encode string to send --> returns byte[]
MteBase.ArrStatus encoded = _encoder.encode(stringToEncode);
if(encoded.status != MteStatus.mte_status_success)
{
	System.err.print("Error encoding. Status: " +MteBase.getStatusName(encoded.status)+ " / " + MteBase.getStatusDescription(encoded.status));
	//... Close socket if needed here
				
	System.out.print("Press enter to exit");
	String end = br.readLine();
	System.exit(0);
}

// show packet sending out --> optional 
// This is for demonstration purposes ONLY and should NOT be done normally
String sending = Base64.getEncoder().encodeToString(encoded.arr);
System.out.println("MTE Packet: " + sending);

//
// Decode incoming message --> returns string
MteBase.StrStatus decoded = _decoder.decodeStr(receivedBytes);
if(decoded.status != MteStatus.mte_status_success)
{
	System.err.print("Error decoding. Status: " +MteBase.getStatusName(decoded.status)+ " / " + MteBase.getStatusDescription(decoded.status));
	//... Close socket if needed here
				
	System.out.print("Press enter to exit");
	String end = br.readLine();
	System.exit(0);
}

```

</ol>

***The Server side and the Client side of the MTE Sockets Tutorial should now be ready for use on your device.***


<div style="page-break-after: always; break-after: page;"></div>

# Contact Eclypses

<img src="Eclypses.png" style="width:8in;"/>

<p align="center" style="font-weight: bold; font-size: 22pt;">For more information, please contact:</p>
<p align="center" style="font-weight: bold; font-size: 22pt;"><a href="mailto:info@eclypses.com">info@eclypses.com</a></p>
<p align="center" style="font-weight: bold; font-size: 22pt;"><a href="https://www.eclypses.com">www.eclypses.com</a></p>
<p align="center" style="font-weight: bold; font-size: 22pt;">+1.719.323.6680</p>

<p style="font-size: 8pt; margin-bottom: 0; margin: 300px 24px 30px 24px; " >
<b>All trademarks of Eclypses Inc.</b> may not be used without Eclypses Inc.'s prior written consent. No license for any use thereof has been granted without express written consent. Any unauthorized use thereof may violate copyright laws, trademark laws, privacy and publicity laws and communications regulations and statutes. The names, images and likeness of the Eclypses logo, along with all representations thereof, are valuable intellectual property assets of Eclypses, Inc. Accordingly, no party or parties, without the prior written consent of Eclypses, Inc., (which may be withheld in Eclypses' sole discretion), use or permit the use of any of the Eclypses trademarked names or logos of Eclypses, Inc. for any purpose other than as part of the address for the Premises, or use or permit the use of, for any purpose whatsoever, any image or rendering of, or any design based on, the exterior appearance or profile of the Eclypses trademarks and or logo(s).
</p>