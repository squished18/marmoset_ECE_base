#!/usr/bin/env python

import commands
import sys

if commands.getoutput("./encrypt 3 c") == "NULL":
    print "passed"
    sys.exit(0)
print "failed"
sys.exit(1)
