del /s /q classes
md classes
del frost.jar

md classes\data

copy data\*.* classes\data

javac -O -classpath classes;source -d classes source\components\*.java
javac -O -classpath classes;source -d classes source\crypt\*.java
javac -O -classpath classes;source -d classes source\fcptools\*.java
javac -O -classpath classes;source -d classes source\ext\*.java
javac -O -classpath classes;source -d classes source\org\bouncycastle\crypto\*.java
javac -O -classpath classes;source -d classes source\org\bouncycastle\crypto\params\*.java
javac -O -classpath classes;source -d classes source\org\bouncycastle\crypto\engines\*.java
javac -O -classpath classes;source -d classes source\org\bouncycastle\crypto\digests\*.java
javac -O -classpath classes;source -d classes source\org\bouncycastle\crypto\signers\*.java
javac -O -classpath classes;source -d classes source\org\bouncycastle\crypto\generators\*.java
javac -O -classpath classes;source -d classes source\org\bouncycastle\util\encoders\*.java
javac -O -classpath classes;source -d classes source\*.java
javac -O -classpath classes;source -d classes source\StartBrowser.java
javac -O -classpath classes;source -d classes source\gui\model\*.java
javac -O -classpath classes;source -d classes source\res\*.java


cd classes
jar -cmf ..\source\frost.manifest ..\frost.jar *.*
cd..

