#!/bin/sh
#
#
#
echo "starting mqtt broker."
JAVA_HOME=/usr
CLASSPATH=.:./conf
${JAVA_HOME}/bin/java -classpath $CLASSPATH -jar mqtt-chat-server-1.0.0.jar
#nohup ${JAVA_HOME}/bin/java -classpath $CLASSPATH -jar target/db-push-pitcher-1.0.0.jar &
