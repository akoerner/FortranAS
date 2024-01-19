#!/usr/bin/env bash

set -euo pipefail

echoerr (){ printf "%s" "$@" >&2;}
exiterr (){ echoerr "$@"; exit 1;}

SCRIPT_DIRECTORY="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
FORTRAN_AS_BUILD_DIRECTORY="$(realpath ${SCRIPT_DIRECTORY}/../..)/build"
SOURCE_DIRECTORY="$(realpath ${SCRIPT_DIRECTORY}/../..)/source"

if [[ ! -d "${FORTRAN_AS_BUILD_DIRECTORY}" ]]; then
    exiterr "ERROR: FortranAS build directory does not exist. Did you build it?" 
fi
cd ${SCRIPT_DIRECTORY}
set -x
rm -rf output*
rm -rf *.sqlite3
${FORTRAN_AS_BUILD_DIRECTORY}/fortranas --input-source-code-directory ${SOURCE_DIRECTORY} -p
python3 subtree_histogram.py
rm -rf output*
rm -rf *.sqlite3
