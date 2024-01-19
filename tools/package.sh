#!/usr/bin/env bash

set -euo pipefail

echoerr (){ printf "%s" "$@" >&2;}
exiterr (){ echoerr "$@"; exit 1;}

SCRIPT_DIRECTORY="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
BUILD_DIRECTORY="$(realpath "${SCRIPT_DIRECTORY}/..")/build"
TOOLS_DIRECTORY="$(realpath "${SCRIPT_DIRECTORY}/..")/tools"
FORTRAN_AS_DIRECTORY="$(realpath "${SCRIPT_DIRECTORY}/..")"

if [[ ! -d "${BUILD_DIRECTORY}" ]]; then
    exiterr "ERROR: BUILD_DIRECTORY: ${BUILD_DIRECTORY} does not exist. Did you build FortranAS?"
fi

cd "${BUILD_DIRECTORY}"
jar_file=$(find "${BUILD_DIRECTORY}" -type f -name 'FortranAS*.jar' -print -quit)
archive_directory=$(basename "$jar_file" .jar)
mkdir -p "${archive_directory}/source"
cp "${FORTRAN_AS_DIRECTORY}/source/README.md" "${archive_directory}/source"
cp "${FORTRAN_AS_DIRECTORY}/documentation/release_readme.md" "${archive_directory}/README.md"

cp "${jar_file}" "${archive_directory}"
cp "sql" "${archive_directory}" -r
cp *.conf "${archive_directory}" -r
mkdir -p "${archive_directory}/tools"
cp "${TOOLS_DIRECTORY}/unifdef.sh" "${archive_directory}/tools"
cp "${TOOLS_DIRECTORY}/dot_converter.sh" "${archive_directory}/tools"
cp -r "${TOOLS_DIRECTORY}/database_analysis" "${archive_directory}/tools"
cp "fortranas" "${archive_directory}" -r
cp "${FORTRAN_AS_DIRECTORY}/code_clone_analysis" "${archive_directory}" -r
tar -czvf "${archive_directory}.tar.gz" "${archive_directory}"
zip -r "${archive_directory}.zip" "${archive_directory}"
