#!/bin/sh
ps -ef | grep "mqtt-chat-server-1.0.0.jar" | grep -v grep | awk '{print $2}' | xargs kill
