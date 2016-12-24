/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Jun 9, 2005
 *
 */
package edu.umd.cs.marmosetdemo;

/**
 * This class implements a Vigenere cipher.  The Vigenere cipher is a polyalphabetic ciper
 * named after Blaise de Vigenere, a 16th century French mathematician.  Unlike a Caeserian 
 * cipher where each letter is substituted for one other letter (and thus susceptible to
 * frequency attacks), a Vigenere cipher can encipher the same letter to many different 
 * letters.    
 * <p>
 * Suppose the key is "fab" and the plain text to be encrypted is "foobar"
 * <p>
 * The cipher works like this:
 * <ul>
 * <li> The first character of ciphertext is formed by shifting the first letter 
 * of the plain text ahead by the number of letters away from 'a' the first character
 * of the key is located.
 * <p>
 * This is a really complicated way of saying that 'f' in the key means to shift ahead 5 spots 
 * (wrapping around from Z to A of course).
 * <br>
 * For our example, this means that the 'f' of "foobar" becomes 'k'.
 * <li> The second character of the ciphertext is formed by shifting the second letter
 * of the plain text ahead by the number of letters away from 'a' the second character
 * of the key is located.
 * <br>
 * For our example, the first 'o' in foobar will stay an 'o' because there are no letters
 * between 'a' and itself.
 * <li> Keep repeating this process until there are no more letters left in the key; then
 * cycle back to the beginning of the key.
 * <li> To decrypt, do the same thing in reverse, meaning that you shift back by number
 * of letters away from 'a' each letter of the key is located.
 * </ul>
 */
public class VigenereCipher
{
    public static final int MAGIC_NUMBER = 97;
    private String key;
    
    /**
     * Construct a new Vigenere cipher that will encrypt plain-text using the given key.
     * @param key The key to be used by this cipher instance for encrypting plain-text.
     */
    public VigenereCipher(String key)
    {
        this.key = key;
    }
    /**
     * Encrypts a piece of plain text using the key this Vigenere cipher 
     * instance's constructor was called with, and returns the encrypted ciper-text.
     * <p>
     * The only permissible characters in the input are [a-zA-Z]. This method throws 
     * an exception if the plain text contains characters not in [a-zA-Z].
     * @param plainText The plain text to be encrypted.
     * @return The ciphertext after encryption.
     * @throws InvalidCharacterException if the plain text contains any
     * characters that are not in [a-zA-Z].
     */
    public String encryptPlainText(String plainText)
    throws InvalidCharacterException
    {
        StringBuffer result = new StringBuffer();
        for (int ii=0; ii < plainText.length(); ii++) {
            char keyChar = key.charAt(ii % key.length());
            result.append(encryptChar(plainText.charAt(ii), keyChar));
        }
        return result.toString();
    }
    
    /**
     * Decrypts a piece of ciphertext using the key this Vigenere cipher 
     * instance's constructor was called with, and returns the plain-text.
     * @param cipherText The cipher text to be decrypted.
     * @return The plain text after decryption.
     * @throws InvalidCharacterException if the cipherText contains any characters that
     * are not in [a-zA-Z].
     */
    public String decryptCipherText(String cipherText)
    throws InvalidCharacterException
    {
        StringBuffer result = new StringBuffer();
        for (int ii=0; ii < cipherText.length(); ii++) {
            char keyChar = key.charAt(ii % key.length());
            result.append(decryptChar(cipherText.charAt(ii), keyChar));
        }
        return result.toString();
    }

    /**
     * Encrypts a character using another characater as the key.  The input characters to
     * this method could be either case, but the encrypted character returned will be lowercase.
     * This method throws an exception if the input characters are not in the range [a-zA-Z]. 
     * @param c The character to be encrypted.
     * @param key The character to use for a key.
     * @return The encrypted character in lowercase.
     * @throws InvalidCharacterException if either the characater or the key are not
     * in [a-zA-Z].
     */
    public static char encryptChar(char c, char key)
    throws InvalidCharacterException
    {
        c = Character.toLowerCase(c);
        key = Character.toLowerCase(key);
        
        c -= MAGIC_NUMBER;
        key -= MAGIC_NUMBER;

        if (c < 0 || c > 25 || key < 0 || key > 25)
            throw new InvalidCharacterException();
        return (char)(((c + key) % 26) + MAGIC_NUMBER);
    }
    
    /**
     * Decrypts a character using another character as the key.  The character to be decrypted 
     * could be either case, but the decrypted character will be lowercase.  The input
     * characters to this method must be in the range [a-zA-Z] or this method will throw
     * an exception.
     * @param c The character to be decrypted.
     * @param key The character to use for a key.
     * @return The decrypted character in lowercase.
     * @throws InvalidCharacterException if either the characater or the key are not
     * in [a-zA-Z]. 
     */
    public static char decryptChar(char c, char key)
    throws InvalidCharacterException
    {
        c = Character.toLowerCase(c);
        key = Character.toLowerCase(key);
        
        if (!Character.isLetter(c) ||
                !Character.isLetter(key))
            throw new InvalidCharacterException();
        
        c -= MAGIC_NUMBER;
        key -= MAGIC_NUMBER;
        
        return (char)(((c + 26 - key) % 26) + MAGIC_NUMBER);
    }
    
    public static void main(String args[])
    throws Exception
    {
        VigenereCipher cipher;
        String cipherText;
        
        cipher = new VigenereCipher("aaa");
        System.out.println(cipher.encryptPlainText("foo"));
        
        cipher = new VigenereCipher("cde");
        System.out.println(cipher.encryptPlainText("patterwxyz"));
        
        cipher = new VigenereCipher("key");
        cipherText = cipher.encryptPlainText("plainText");
        System.out.println(cipherText);
        System.out.println(cipher.decryptCipherText(cipherText));
        
        cipher = new VigenereCipher("qwerty");
        cipherText = cipher.encryptPlainText("encryptionisfun");
        System.out.println(cipherText);
        System.out.println(cipher.decryptCipherText(cipherText));
        
        System.out.println(encryptChar('t', 'k'));
        System.out.println(decryptChar('a', 'd'));
    }
}
