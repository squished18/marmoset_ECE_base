#include <stdio.h>
#include <stdlib.h>
#include "vigenere.h"

int main(int argc, char** argv)
{
    char cipherText[80];
    int cipherTextLen;
    char key[80];
    int keyLen;
    int ii;
    char *plainText;

    /*
    for (ii=0; ii < argc; ii++) {
	printf("%d = %s\n", ii, argv[ii]);
    }
    */

    cipherTextLen=strlen(argv[1]);
    keyLen=strlen(argv[2]);

    strncpy(cipherText, argv[1], cipherTextLen);
    strncpy(key, argv[2], keyLen);

    plainText = decrypt(cipherText, cipherTextLen, key, keyLen);

    if (plainText) {
	printf("%s\n", plainText);
	free(plainText);
    } else {
	printf("NULL\n");
    }
}
