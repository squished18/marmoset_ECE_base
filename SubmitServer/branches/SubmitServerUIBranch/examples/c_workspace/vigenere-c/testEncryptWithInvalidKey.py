#!/usr/bin/env python

import commands
import sys

if commands.getoutput("./encrypt a 99") == "NULL":
    print "passed"
    sys.exit(0)
print "failed"
sys.exit(1)
