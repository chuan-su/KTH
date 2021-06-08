#!/bin/bash

mvn compile exec:java -Dexec.mainClass="hw1.ChatServer"  -Dexec.arguments="5000"
