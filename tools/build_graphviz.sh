#!/usr/bin/env bash

# This script will build and install graphviz on your local system
# It assumes you are on a debian based system.


set -euo pipefail

echoerr (){ printf "%s" "$@" >&2;}
exiterr (){ printf "%s\n" "$@" >&2; exit 1;}

SCRIPT_DIRECTORY="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

GRAPHVIZ_VERSION=9.0.0
GRAPHVIZ_URL=https://gitlab.com/api/v4/projects/4207231/packages/generic/graphviz-releases/${GRAPHVIZ_VERSION}/graphviz-${GRAPHVIZ_VERSION}.tar.gz

mkdir -p "${SCRIPT_DIRECTORY}/graphviz"
cd "${SCRIPT_DIRECTORY}/graphviz"

sudo apt-get update 
sudo apt-get install -y build-essential autoconf automake bison flex libtool pkg-config tcl-dev libexpat1-dev wget

wget "${GRAPHVIZ_URL}"
tar xzf *.tar.gz 
cd "graphviz-${GRAPHVIZ_VERSION}"
./configure --with-cairo
make
sudo make install

