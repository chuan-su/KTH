#!/bin/sh

docker run --rm -it -e JUPYTER_ENABLE_LAB=yes -v "$PWD":/home/jovyan/work jupyter/datascience-notebook /bin/bash
