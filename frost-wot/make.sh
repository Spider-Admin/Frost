#!/bin/bash
echo -n "Tidying up: "
rm -rf classes
mkdir classes
rm frost.jar 2> /dev/null
cp -R data classes/
export CLASSPATH=classes:source:.
echo "Done"
echo -n "Compiling: "
javac -O -d classes source/frost/frost.java && \
javac -O -d classes source/res/*.java && \
echo "Done" || (echo "Failed" && exit)
cd classes
echo -n "Building .jar: "
jar -cmf ../source/frost.manifest ../frost.jar * && \
echo "Done" || echo "Failed"
