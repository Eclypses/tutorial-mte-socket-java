//******************************************************************************
// The MIT License (MIT)
//
// Copyright (c) Eclypses, Inc.
//
// All rights reserved.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//******************************************************************************
package com.eclypses.ecdh;



import java.util.Arrays;



public class EcdhP256 {

  public interface EntropyCallback {
    int entropyCallback(byte[] entropyInput);
  }
  
  public static final int Success = 0;
  public static final int RandomFail = -1;
  public static final int InvalidPubKey = -2;
  public static final int InvalidPrivKey = -3;
  public static final int MemoryFail = -4;
  
  public static final int SzPublicKey = 64;
  public static final int SzPrivateKey = 32;
  public static final int SzSecretData = 32;

  
  
  public int createKeyPair(byte[] publicKey) {
    //---------------------------------------------
    // Check if the keys have already been created. 
    //---------------------------------------------
    if (!haveKeys) {
      // Check if publicKey is big enough to receive a raw key. We know that
      // myPublicKey is big enough for that.
      if (publicKey.length < SzPublicKey)
        return MemoryFail;
      // Create the private and public keys.
      int status = jniCreateKeyPair(myPrivateKey, myPublicKey,
                                    (myEntropyCb != null) | (myEntropyInput != null));
      if (status != Success)
        return status;
      myEntropyInput = null;
      haveKeys = true;
    }
    System.arraycopy(myPublicKey, 0, publicKey, 0, myPublicKey.length);
    return Success;
  }
  
  
  
  public int getSharedSecret(byte[] peerPublicKey, byte[] secret) {
    //------------------------------------------------------
    // If the private key has not been set, return an error.
    //------------------------------------------------------
    if (!haveKeys)
      return InvalidPrivKey;
    //-----------------------------------------------------
    // Check if the result buffer would hold a P256 secret.
    //-----------------------------------------------------
    if (secret.length < SzSecretData)
      return MemoryFail;
    //------------------------------------------
    // Check if peerPublicKey is the right size.
    //------------------------------------------
    if (peerPublicKey.length != SzPublicKey)
      return InvalidPubKey;
    //----------------------
    // Create shared secret.
    //----------------------
    int status = jniGetSharedSecret(myPrivateKey, peerPublicKey, secret);
    //---------------------------------------------------
    // Zeroize the private and public keys;
    // reset "haveKeys", whether this just worked or not.
    //---------------------------------------------------
    Arrays.fill(myPrivateKey, (byte) 0);
    Arrays.fill(myPublicKey, (byte) 0);
    haveKeys = false;
    return status;
  }
  
  
  
  public static int getRandom(byte[] random) {
    return jniRandom(random);
  }
  
  
  
  public static void zeroize(byte[] dest) {
    jniZeroize(dest);
  }    
    
  
  
  public int setEntropy(byte[] entropyInput) {
    if (entropyInput.length != SzPrivateKey)
      return MemoryFail;
    myEntropyInput = entropyInput;
    return Success;
  }

  
  
  public void setEntropyCallback(EntropyCallback entropyCB) {
    myEntropyCb = entropyCB;
  }
  
  
  //----------------------------------------------------------------------------
  // Our internal default callback function. It is called directly from the JNI
  // layer.
  //----------------------------------------------------------------------------
  protected int entropyCallback() {
    // Check if a user callback has been set.
    if (myEntropyCb != null) {
      // If we don't have a valid "myEntropyInput" byte[],
      // then create one.
      if (myEntropyInput == null)
        myEntropyInput = new byte[SzSecretData];
      // Call the user callback.
      int status = myEntropyCb.entropyCallback(myEntropyInput);
      // If the user callback does not return with
      // "Success" then we are done here.
      if (status != Success)
        return status;
    }
    // Check if we have entropy input.
    if (myEntropyInput == null)
      return InvalidPrivKey;
    // Check the lengths.
    if (myEntropyInput.length != SzPrivateKey)
      return InvalidPrivKey;
    // myEntropyInput will be copied and zeroized in the jni caller.
    return Success;
  }

  
  
  private byte[] myPrivateKey = new byte[SzPrivateKey];
  private byte[] myPublicKey = new byte[SzPublicKey];
  private boolean haveKeys = false;
  private EntropyCallback myEntropyCb = null;
  private byte[] myEntropyInput = null;

  
  
  // Static initializer.
  static {
    // Load the JNI library.
    System.loadLibrary("mtesupport-ecdh");
    jniInit();
  }
  
  
  
  // Library functions.
  private static native void jniInit();
  private native int jniCreateKeyPair(byte[] privateKey, byte[] publicKey,
                                      boolean doCallback);
  private native int jniGetSharedSecret(byte[] privateKey,
                                        byte[] peerPublicKey,
                                        byte[] secret);
  private static native int jniRandom(byte[] output);
  private static native void jniZeroize(byte[] dest);
}
