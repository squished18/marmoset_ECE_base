#!/usr/bin/env python

import commands
import sys

if commands.getoutput("./decrypt b f") == "w":
    print "passed"
    sys.exit(0)
print "failed"
sys.exit(1)
