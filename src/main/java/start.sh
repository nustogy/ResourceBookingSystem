#!/bin/bash

./compile.sh

# Start 3 network nodes
java NetworkNode -ident 1 -tcpport 9000 A:4 &
sleep 1
java NetworkNode -ident 2 -tcpport 9001 -gateway localhost:9000 B:2 &
sleep 1
java NetworkNode -ident 3 -tcpport 9002 -gateway localhost:9000 C:2 &
