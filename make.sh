#!/bin/bash
echo -n "Tidying up: "
rm -rf classes
mkdir classes
rm frost.jar 2> /dev/null
cp -R data classes/
export CLASSPATH=classes:source:.
echo "Done"
echo -n "Compiling: "
javac -O -d classes source/frost/*.java && \
javac -O -d classes source/frost/gui/*.java && \
javac -O -d classes source/frost/FcpTools/*.java && \
javac -O -d classes source/frost/crypt/*.java && \
javac -O -d classes source/frost/gui/model/*.java && \
javac -O -d classes source/frost/ext/*.java && \
javac -O -d classes source/frost/components/*.java && \
javac -O -d classes source/res/*.java && \
javac -O -d classes source/org/bouncycastle/crypto/*.java && \
javac -O -d classes source/org/bouncycastle/util/encoders/*.java && \
javac -O -d classes source/org/bouncycastle/crypto/digests/*.java && \
javac -O -d classes source/org/bouncycastle/crypto/engines/*.java && \
javac -O -d classes source/org/bouncycastle/crypto/generators/*.java && \
javac -O -d classes source/org/bouncycastle/crypto/paddings/*.java && \
javac -O -d classes source/org/bouncycastle/crypto/params/*.java && \
javac -O -d classes source/org/bouncycastle/crypto/signers/*.java && \
javac -O -d classes source/fillament/util/*.java && \
echo "Done" || (echo "Failed" && exit)
cd classes
echo -n "Building .jar: "
jar -cmf ../source/frost.manifest ../frost.jar * && \
echo "Done" || echo "Failed"
