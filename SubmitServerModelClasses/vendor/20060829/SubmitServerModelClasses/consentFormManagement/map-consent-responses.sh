#!/bin/bash

runsql find.sql > out
./go.py out > foo.sql
runsql foo.sql
