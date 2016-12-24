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
public class PublicTests extends TestCase
{

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(PublicTests.class);
    }
    
    public void testSimpleEncryptChar()
    throws Exception
    {
        char encryptedChar = VigenereCipher.encryptChar('a', 'd');
        assertEquals('d', encryptedChar);
    }
    
    public void testSimpleDecryptChar()
    throws Exception
    {
        char plainChar = VigenereCipher.decryptChar('d', 'd');
        assertEquals('a',plainChar);
    }
    
    public void testWraparoundDecryptChar()
    throws Exception
    {
        char plainChar = VigenereCipher.decryptChar('a', 'd');
        assertEquals('x', plainChar);
    }
    
    public void testWraparoundEncryptChar()
    throws Exception
    {
        char plainChar = VigenereCipher.encryptChar('x', 'd');
        assertEquals('a', plainChar);
    }

}
