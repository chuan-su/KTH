#!/bin/sh

## java -cp classes -Xmx1g ir.Engine -d /info/DD2476/ir19/lab/davisWiki -l ir19.gif -p patterns.txt -ni

./gradlew run --args='-d davisWiki -l ir19.gif -p patterns.txt -ni'
