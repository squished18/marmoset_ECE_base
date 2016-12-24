#ifndef _VIGENERE_H_
#define _VIGENERE_H_

/**
 * Encrypt a piece of plain text with the given key, returning a newly malloc'd
 * cipher text.
 *
 * Should return NULL to signal an error if any of the characters in the plainText
 * are non-alpha-numeric (i.e. A-Za-Z).
 *
 * Before encrypting, all upper-case letters should be converted to lower-case.
 */
char* encrypt(char *plainText, int plainTextLen, char *key, int keyLen);

/**
 * Decrypt a piece of cipher text with the given key, returning a newly malloc'd
 * plain text.
 *
 * Should return NULL to signal an error if any of the characters in the cipher text
 * are non-alpha-numeric (i.e. A-Za-Z).
 *
 * Before decrypting, all upper-case letters should be converted to lower-case.
 */
char* decrypt(char *cipherText, int cipherTextLen, char *key, int keyLen);

#endif
