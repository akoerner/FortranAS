#!/usr/bin/env bash

# This script extracts the contents of a java .jar file for debugging purposes.

set -euo pipefail
#set -euxo pipefail #debug mode

echoerr (){ printf "%s" "$@" >&2;}
exiterr (){ printf "%s\n" "$@" >&2; exit 1;}

SCRIPT_DIRECTORY="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
FORTRAN_AS_BASE_DIRECTORY=$(realpath "${SCRIPT_DIRECTORY}/..")
BUILD_DIRECTORY="${FORTRAN_AS_BASE_DIRECTORY}/build"

mkdir -p "${BUILD_DIRECTORY}/jar"
cp "${BUILD_DIRECTORY}"/FortranAS-*.jar "${BUILD_DIRECTORY}/jar"

(
cd "${BUILD_DIRECTORY}/jar"
jar -xf FortranAS-*.jar
)
