#!/bin/sh

## 3-gram
./gradlew kgramTest --args=' -f files/kgram_test.txt -p patterns.txt -k 3 -kg "abb"'
./gradlew kgramTest --args=' -f files/kgram_test.txt -p patterns.txt -k 3 -kg "chi ren"'

## 2-gram
# ./gradlew kgramTest --args=' -f files/kgram_test.txt -p patterns.txt -k 2 -kg "th he"'
# ./gradlew kgramTest --args=' -f files/kgram_test.txt -p patterns.txt -k 2 -kg "ve"'