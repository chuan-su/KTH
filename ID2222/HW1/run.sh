#!/bin/bash
for i in "$@"
do
case $i in
    -k=*|--kshingle=*)
    KSHINGLE="${i#*=}"
    shift # past argument=value
    ;;
    -n=*|--nsignature=*)
    NSIGNATURE="${i#*=}"
    shift # past argument=value
    ;;
    -b=*|--band=*)
    BAND="${i#*=}"
    shift # past argument=value
    ;;
    -r=*|--row=*)
    ROW="${i#*=}"
    shift # past argument with no value
    ;;
    *)
    ;;
esac
done
echo "k-shingle    = ${KSHINGLE}"
echo "n-signature  = ${NSIGNATURE}"
echo "band         = ${BAND}"
echo "row          = ${ROW}"

exec spark-submit \
     --class "hw1.Main" \
     --master local[4] \
     target/scala-2.11/hw1_2.11-1.0.jar \
     ${KSHINGLE} ${NSIGNATURE} ${BAND} ${ROW}
