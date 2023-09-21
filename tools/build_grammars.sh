#!/usr/bin/env bash

set -euo pipefail

echoerr (){ printf "%s" "$@" >&2;}
exiterr (){ printf "%s\n" "$@" >&2; exit 1;}

SCRIPT_DIRECTORY="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
OUTPUT_DIRECTORY="$(realpath "${SCRIPT_DIRECTORY}/../FortranAS/src/generated")"
ANTLR4_DIRECTORY="$(realpath "${SCRIPT_DIRECTORY}/../antlr4")"

cd "${ANTLR4_DIRECTORY}"
if [ -f /.docker ]; then
    echo "Cannot build grammars in docker, skipping build of grammars.";
else
    echo "Building Antlr4 grammars...";
    (cd "${ANTLR4_DIRECTORY}" && make)
fi

cp "${ANTLR4_DIRECTORY}/generated" "${OUTPUT_DIRECTORY}/antlr4" -r

