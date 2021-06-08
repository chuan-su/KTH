#!/bin/bash
for i in "$@"
do
case $i in
    -s=*|--support=*)
    SUPPORT="${i#*=}"
    shift # past argument=value
    ;;
    -k=*|--nitems=*)
    KITEMS="${i#*=}"
    shift # past argument=value
    ;;
    -c=*|--band=*)
    CONFIDENCE="${i#*=}"
    shift # past argument=value
    ;;
    *)
    ;;
esac
done
echo "k-itemset    = ${KITEMS}"
echo "s-support    = ${SUPPORT}"
echo "c-confidence = ${CONFIDENCE}"

exec spark-submit \
     --class "hw2.Main" \
     --master local[4] \
     target/scala-2.11/hw2_2.11-1.0.jar \
     ${KITEMS} ${SUPPORT} ${CONFIDENCE}
