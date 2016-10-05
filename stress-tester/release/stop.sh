#!/bin/sh
ps -ef | grep "stress-tester-1.0.0.jar" | grep -v grep | awk '{print $2}' | xargs kill
