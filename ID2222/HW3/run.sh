#!/bin/bash
for i in "$@"
do
case $i in
    -m=*|--memory=*)
    MEMORY="${i#*=}"
    shift # past argument=value
    ;;
    -alg=*|--algorithm=*)
    ALGORITHM="${i#*=}"
    shift # past argument=value
    ;;
    *)
    ;;
esac
done
echo "m-memory      = ${MEMORY}"
echo "alg-algorithm = ${ALGORITHM}"

exec spark-submit \
     --class "hw3.Main" \
     --master local[4] \
     target/scala-2.11/hw3_2.11-1.0.jar \
     ${MEMORY} ${ALGORITHM}