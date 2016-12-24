#!/usr/bin/env python

import commands
import sys

original="panic"
cipher="rdrk"

encrypted=commands.getoutput("./encrypt %s %s" % (original, cipher))

if encrypted != "gdest":
    print "failed"
    sys.exit(1)

decrypted=commands.getoutput("./decrypt %s %s" % (encrypted, cipher))

if decrypted == original:
    print "passed"
    sys.exit(0)
print "failed"
sys.exit(1)
