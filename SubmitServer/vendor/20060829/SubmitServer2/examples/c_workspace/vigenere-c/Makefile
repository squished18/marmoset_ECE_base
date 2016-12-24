LDFLAGS = -g 
CFLAGS = -Wall -g -ansi -Werror
CC = gcc

PUBLIC_TESTS = testSimpleEncrypt.py testSimpleDecrypt.py \
	testWraparoundEncrypt.py testWraparoundDecrypt.py
RELEASE_TESTS = testEncryptInvalidChar.py testInversion.py testKeyLongerThanPlainText.py \
	testEncryptWithInvalidKey.py
SECRET_TESTS =
ALL_TESTS = $(PUBLIC_TESTS) $(RELEASE_TESTS) $(SECRET_TESTS)

EXE = encrypt.c decrypt.c vigenere.h 

all:	clean $(ALL_TESTS) encrypt decrypt
	chmod +x $(ALL_TESTS) encrypt decrypt

setup:		$(ALL_TESTS) Makefile $(EXE) test.properties vigenere.c Student.Makefile
		jar cf setup.jar Makefile $(ALL_TESTS) $(EXE) test.properties Student.Makefile

starterfiles:	$(PUBLIC_TESTS) $(EXE) Student.Makefile
		/bin/rm -rf starter-files
		mkdir starter-files
		cp Student.Makefile starter-files/Makefile
		cp Student.vigenere.c starter-files/vigenere.c
		cp $(PUBLIC_TESTS) $(EXE) starter-files/
		jar cf starter-files.jar starter-files/*

canonical:	vigenere.c
		jar cf canonical.jar vigenere.c

clean:
	/bin/rm -rf *.o encrypt decrypt starter-files *jar

vigenere.o: vigenere.c vigenere.h
	$(CC) -c $(LDFLAGS) $<

encrypt: encrypt.c vigenere.h vigenere.o
	$(CC) $< vigenere.o -o $@

decrypt: decrypt.c vigenere.h vigenere.o
	$(CC) $< vigenere.o -o $@

check:	vigenere.o
	echo $<
	echo $@
	echo "public: $(PUBLIC_TESTS)"
	echo "release: $(RELEASE_TESTS)"
