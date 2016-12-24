package edu.umd.cs.marmosetdemo;

import junit.framework.TestCase;

public class SecretTests extends TestCase
{
    public void testEncryptDecryptLongString()
    throws Exception
    {
        // These values are hard-coded.  If I were clever I'd include my own implementation
        // and use that encrypt/decrypt.
        VigenereCipher cipher = new VigenereCipher("qwertyuiop");
        String cipherText = cipher.encryptPlainText("supercalafragelisticespiabadocious");
        assertEquals("iqtvkautouhwkvegmbwruotztzulcrykyj", cipherText);
        
        String plainText = cipher.decryptCipherText("iqtvkautouhwkvegmbwruotztzulcrykyj");
        assertEquals("supercalafragelisticespiabadocious", plainText);
    }
}
