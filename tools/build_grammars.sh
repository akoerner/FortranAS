#!/usr/bin/env bash

set -euo pipefail

echoerr (){ printf "%s" "$@" >&2;}
exiterr (){ printf "%s\n" "$@" >&2; exit 1;}

SCRIPT_DIRECTORY="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
FORTRAN_AS_BASE_DIRECTORY=$(realpath "${SCRIPT_DIRECTORY}/..")
SOURCE_DIRECTORY="${FORTRAN_AS_BASE_DIRECTORY}/FortranAS/src"
TEMPLATE_DIRECTORY="${SOURCE_DIRECTORY}/org/fortranas/antlr4/templates"
OUTPUT_DIRECTORY="${SOURCE_DIRECTORY}/org/fortranas/antlr4/generated/fortran"
ANTLR4_DIRECTORY="${FORTRAN_AS_BASE_DIRECTORY}/antlr4"


cd "${ANTLR4_DIRECTORY}"
if [ -f /.docker ]; then
    echo "Cannot build grammars in docker, skipping build of grammars.";
else
    echo "Building Antlr4 grammars...";
    (cd "${ANTLR4_DIRECTORY}" && make)
fi

mkdir -p "${OUTPUT_DIRECTORY}"
echo "OUTPUT_DIRECTORY: ${OUTPUT_DIRECTORY}"
cp "${ANTLR4_DIRECTORY}/generated/fortran"/* "${OUTPUT_DIRECTORY}" -r

