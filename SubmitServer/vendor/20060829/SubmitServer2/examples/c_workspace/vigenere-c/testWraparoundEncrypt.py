#!/usr/bin/env python

import commands
import sys

if commands.getoutput("./encrypt z c") == "b":
    print "passed"
    sys.exit(0)
print "failed"
sys.exit(1)
