#!/usr/bin/env bash

set -euo pipefail
#set -euxo pipefail #debug mode

echoerr (){ printf "%s" "$@" >&2;}
exiterr (){ printf "%s\n" "$@" >&2; exit 1;}

SCRIPT_DIRECTORY="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
FORTRAN_AS_BASE_DIRECTORY=$(realpath "${SCRIPT_DIRECTORY}/..")
SOURCE_DIRECTORY="${FORTRAN_AS_BASE_DIRECTORY}/FortranAS/src"
TEMPLATE_DIRECTORY="${SOURCE_DIRECTORY}/org/fortranas/antlr4/templates"
OUTPUT_DIRECTORY="${SOURCE_DIRECTORY}/org/fortranas/antlr4/generated"
BUILD_DIRECTORY="${FORTRAN_AS_BASE_DIRECTORY}/build"

echo "Cleaning OUTPUT_DIRECTORY: ${OUTPUT_DIRECTORY}"
rm -rf "${OUTPUT_DIRECTORY}"
echo "Cleaning BUILD_DIRECTORY: ${BUILD_DIRECTORY}"
rm -rf "${BUILD_DIRECTORY}"
