#!/bin/sh
JARDIR=.
CLASSPATH="$JARDIR/frost.jar:$JARDIR/lib/fecImpl.jar:$JARDIR/lib/genChkImpl.jar:$JARDIR/lib/xercesImpl.jar:$JARDIR/lib/xml-apis.jar:$JARDIR/lib/skinlfFix.jar:$JARDIR/lib/skinlf.jar:$JARDIR/lib/BCastle.jar:$CLASSPATH"
export CLASSPATH
java frost.frost "$@"
