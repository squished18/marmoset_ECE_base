#include <stdio.h>
#include <stdlib.h>
#include "vigenere.h"

int main(int argc, char** argv)
{
    char plainText[80];
    int plainTextLen;
    char key[80];
    int keyLen;
    int ii;
    char *cipherText;

    /*
    for (ii=0; ii < argc; ii++) {
	printf("%d = %s\n", ii, argv[ii]);
    }
    */

    plainTextLen=strlen(argv[1]);
    keyLen=strlen(argv[2]);

    strncpy(plainText, argv[1], plainTextLen);
    strncpy(key, argv[2], keyLen);

    cipherText = encrypt(plainText, plainTextLen, key, keyLen);

    if (cipherText) {
	printf("%s\n", cipherText);
	free(cipherText);
    } else {
	printf("NULL\n");
    }
}
