#!/bin/sh

JARDIR=.

CLASSPATH="$JARDIR/frost.jar:$CLASSPATH"
export CLASSPATH

java -server frost "$@"
