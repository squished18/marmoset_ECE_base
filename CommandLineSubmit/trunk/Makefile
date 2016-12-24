SHELL=/bin/bash
BIN=/u/cs_build/bin
EXEC=marmoset_submit
SRC=marmoset_submit.c
JAR=marmoset_submit.jar
CC=gcc
CFLAGS=

.PHONY: default $(EXEC) permissions install clean

default: ${EXEC} permissions

$(EXEC): $(SRC)
	$(CC) $(CFLAGS) -o $@ $<
	ant

permissions: $(EXEC)
	chgrp cs_build $(EXEC)
	chmod 2751     $(EXEC) 
	chgrp cs_build $(JAR)
	chmod 640      $(JAR)

install: $(EXEC) permissions
	cp -p $(EXEC) $(BIN)/
	cp -p $(JAR)  $(BIN)/

clean :
	ant clean
	rm $(EXEC)
