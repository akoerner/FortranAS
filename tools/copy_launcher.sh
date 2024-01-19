#!/usr/bin/env bash

set -euo pipefail

echoerr (){ printf "%s" "$@" >&2;}
exiterr (){ printf "%s\n" "$@" >&2; exit 1;}

SCRIPT_DIRECTORY="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
BUILD_DIRECTORY="$(realpath "${SCRIPT_DIRECTORY}/../build")"
cp "${SCRIPT_DIRECTORY}/fortranas" "${BUILD_DIRECTORY}"
exit 1
cp "${SCRIPT_DIRECTORY}/../sql" "${BUILD_DIRECTORY}/" -r
