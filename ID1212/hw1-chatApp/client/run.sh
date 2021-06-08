#!/bin/bash

mvn compile exec:java -Dexec.mainClass="hw1.ChatClient"  -Dexec.arguments="localhost,5000"
