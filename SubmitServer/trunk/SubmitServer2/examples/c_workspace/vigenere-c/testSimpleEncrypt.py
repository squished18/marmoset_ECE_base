#!/usr/bin/env python

import commands
import sys

if commands.getoutput("./encrypt a c") == "c":
    print "passed"
    sys.exit(0)
print "failed"
sys.exit(1)
