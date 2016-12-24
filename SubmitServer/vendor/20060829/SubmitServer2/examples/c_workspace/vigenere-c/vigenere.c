#include "vigenere.h"
#include <stdio.h>
#include <stdlib.h>

#define MAGIC_NUMBER 97;

char toLowerCase(char c)
{
    if (c >= 65 && c <= 90) {
	return c+32;
    }
    if (c >= 97 && c <= 122) {
	return c;
    }
    return '\0';
}

char encryptChar(char c, char key)
{
    c=toLowerCase(c);
    key=toLowerCase(key);
    if (c=='\0' || key=='\0') {
	return '\0';
    }

    c-=MAGIC_NUMBER;
    key-=MAGIC_NUMBER;

    return (char)((c+key)%26)+MAGIC_NUMBER;
}

char decryptChar(char c, char key)
{
    c=toLowerCase(c);
    key=toLowerCase(key);
    if (c=='\0' || key=='\0') {
	return '\0';
    }

    c-=MAGIC_NUMBER;
    key-=MAGIC_NUMBER;

    return (char)((c + 26 - key) % 26) + MAGIC_NUMBER;
}


char* encrypt(char *plainText, int textLen, char *key, int keyLen)
{
    char * cipherText=(char*)malloc(textLen+1);
    int ii;
    for (ii=0; ii<textLen; ii++) {
	char c=encryptChar(plainText[ii], key[ii%keyLen]);
	if (c=='\0') {
	    free(cipherText);
	    return NULL;
	}
	cipherText[ii] = c;
    }
    cipherText[textLen]='\0';
    return cipherText;
}


char* decrypt(char *cipherText, int textLen, char *key, int keyLen)
{
    char * plainText=(char*)malloc(textLen+1);
    int ii;
    for (ii=0; ii<textLen; ii++) {
	char c=decryptChar(cipherText[ii], key[ii%keyLen]);
	if (c=='\0') {
	    free(cipherText);
	    return NULL;
	}
	plainText[ii] = c;
    }
    plainText[textLen]='\0';
    return plainText;
}


