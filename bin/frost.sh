#!/bin/sh
JARDIR=.
CLASSPATH="$JARDIR/frost.jar:$JARDIR/lib/fecImpl.jar:$JARDIR/lib/genChkImpl.jar:$JARDIR/lib/xercesImpl.jar:$JARDIR/lib/xml-apis.jar:$JARDIR/lib/skinlf.jar:$CLASSPATH"
export CLASSPATH
java frost.frost "$@"
