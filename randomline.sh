#!/bin/bash

RAND=`cat /proc/sys/kernel/random/uuid | cut -c1-4 | od -d | head -1 | cut -d' ' -f2`
LINES=`cat "$1" | wc -l`
LINE=`expr $RAND % $LINES + 1`
head -$LINE $1 | tail -1
