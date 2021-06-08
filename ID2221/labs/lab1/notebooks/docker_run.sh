#!/bin/sh

docker run --rm -p 4567:8888 -v "$PWD":/home/jovyan/work jupyter/all-spark-notebook
