#!/bin/sh
#
#
#
echo "starting mqtt broker."
JAVA_HOME=/usr
CLASSPATH=.:./conf
nohup ${JAVA_HOME}/bin/java -classpath $CLASSPATH -jar stress-tester-1.0.0.jar &

