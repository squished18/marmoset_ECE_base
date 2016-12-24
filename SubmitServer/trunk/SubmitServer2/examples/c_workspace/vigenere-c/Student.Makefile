LDFLAGS = -g 
CFLAGS = -Wall -g -ansi -Werror
CC = gcc

PUBLIC_TESTS = testSimpleEncrypt.py testSimpleDecrypt.py \
	testWraparoundEncrypt.py testWraparoundDecrypt.py
ALL_TESTS = $(PUBLIC_TESTS)

all:	clean $(ALL_TESTS) encrypt decrypt
	chmod +x $(ALL_TESTS) encrypt decrypt
	./testSimpleEncrypt.py
	./testSimpleDecrypt.py
	./testWraparoundEncrypt.py
	./testWraparoundDecrypt.py

clean:
	rm -f *.o encrypt decrypt

vigenere.o: vigenere.c vigenere.h
	$(CC) -c $(LDFLAGS) $<

encrypt: encrypt.c vigenere.h vigenere.o
	$(CC) $< vigenere.o -o $@

decrypt: decrypt.c vigenere.h vigenere.o
	$(CC) $< vigenere.o -o $@
