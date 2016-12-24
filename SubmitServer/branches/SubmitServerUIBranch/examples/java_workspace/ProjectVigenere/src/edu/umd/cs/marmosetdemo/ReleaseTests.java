/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Jun 9, 2005
 *
 */
package edu.umd.cs.marmosetdemo;

import junit.framework.TestCase;

/**
 * @author jspacco
 *
 */
public class ReleaseTests extends TestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ReleaseTests.class);
    }

    public void testEncryptInvalidChar()
    throws Exception
    {
        try {
            VigenereCipher.encryptChar('q', '7');
        } catch (InvalidCharacterException e) {
            return;
        }
        assertTrue(false);
    }
    
    public void testDecryptInvalidChar()
    throws Exception
    {
        try {
            VigenereCipher.decryptChar('_', 'd');
        } catch (InvalidCharacterException e) {
            return;
        }
        assertTrue(false);
    }
    
    public void testInverseOperation()
    throws Exception
    {
        char plainChar = 'x';
        char key = 'w';
        char cipherChar = VigenereCipher.encryptChar(plainChar, key);
        char result = VigenereCipher.decryptChar(cipherChar, key);
        assertEquals(plainChar, result);
    }
    
    public void testNullEncryptChar()
    throws Exception
    {
        char plainChar = 'm';
        char key = 'a';
        char cipherChar = VigenereCipher.encryptChar(plainChar, key);
        assertEquals(plainChar, cipherChar);
    }
    
    public void testEncryptSimpleString()
    throws Exception
    {
        // These values are hard-coded.  If I were clever I'd include my own implementation
        // and use that encrypt/decrypt.
        VigenereCipher cipher = new VigenereCipher("key");
        String cipherText = cipher.encryptPlainText("plainText");
        System.out.println("cipherText is " +cipherText);
        assertEquals("zpysrrobr", cipherText);
    }
    
    public void testDecryptSimpleString()
    throws Exception
    {
        // These values are hard-coded.  If I were clever I'd include my own implementation
        // and use that encrypt/decrypt.
        VigenereCipher cipher = new VigenereCipher("qwerty");
        String plainText = cipher.decryptCipherText("ujgirnjesebqvqr");
        System.out.println("plainText is " +plainText);
        assertEquals("encryptionisfun", plainText);
    }
    
    public void testEncryptInvalidString()
    throws Exception
    {
        VigenereCipher cipher = new VigenereCipher("qwerty");
        
        try {
            cipher.encryptPlainText("asdf1234");
        } catch (InvalidCharacterException e) {
            return;
        }
        assertTrue(false);
    }
}
