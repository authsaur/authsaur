#!/bin/bash
nohup java -XX:+HeapDumpOnOutOfMemoryError -jar cas.war > cas.log &
tail -f cas.log