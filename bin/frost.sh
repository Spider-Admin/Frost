#!/bin/sh

#
# resolve symlinks
#

PRG=$0

while [ -h "$PRG" ]; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '^.*-> \(.*\)$' 2>/dev/null`
    if expr "$link" : '^/' 2> /dev/null >/dev/null; then
        PRG="$link"
    else
        PRG="`dirname "$PRG"`/$link"
    fi
done

PROGDIR=`dirname "$PRG"`

cd $PROGDIR

if [ "`uname`" = "Darwin" ]; then
    ADDFLAGS='-Dapple.laf.useScreenMenuBar=true -Xdock:name=Frost'
else
    ADDFLAGS=''
fi

java -Xmx96M $ADDFLAGS -jar frost.jar "$@"
