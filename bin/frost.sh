#!/bin/sh

JARDIR=.

CLASSPATH="$JARDIR/frost.jar:$JARDIR/lib/xercesImpl.jar:$JARDIR/lib/xml-apis.jar:$JARDIR/lib/xmlParserAPIs.jar:$CLASSPATH"
export CLASSPATH

java frost.frost "$@"
