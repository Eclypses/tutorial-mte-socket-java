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

package com.tutorial;

import com.eclypses.ecdh.EcdhP256;

public class MteSetupInfo {
    private EcdhP256 ecdhManager;
    private byte[] personalization = null;
    private byte[] nonce = null;
    private byte[] publicKey = null;
    private byte[] peerKey = null;

    public MteSetupInfo() throws Exception {
	this.ecdhManager = new EcdhP256();

	// Set public key size.
	this.publicKey = new byte[EcdhP256.SzPublicKey];

	// Create the private and public keys.
	int res = this.ecdhManager.createKeyPair(this.publicKey);
	if (res < 0) {
	    throw new Exception(String.valueOf(res));
	}
    }

    public byte[] getSharedSecret() throws Exception {
	if (peerKey.length == 0) {
	    throw new Exception(String.valueOf(EcdhP256.MemoryFail));
	}

	// Create temp byte array.
	byte[] temp = new byte[EcdhP256.SzSecretData];

	// Create shared secret.
	int res = this.ecdhManager.getSharedSecret(peerKey, temp);
	if (res < 0) {
	    throw new Exception(String.valueOf(EcdhP256.MemoryFail));
	}

	return temp;
    }

    public byte[] getPublicKey() {
	return this.publicKey;
    }

    public void setPersonalization(byte[] data) {
	this.personalization = data;
    }

    public byte[] getPersonalization() {
	return this.personalization;
    }

    public void setNonce(byte[] data) {
	this.nonce = data;
    }

    public byte[] getNonce() {
	return this.nonce;
    }

    public void setPeerPublicKey(byte[] data) {
	this.peerKey = data;
    }

    public byte[] getPeerPublicKey() {
	return this.peerKey;
    }
}
