#!/usr/bin/env bash

set -euo pipefail

echoerr (){ printf "%s" "$@" >&2;}
exiterr (){ printf "%s\n" "$@" >&2; exit 1;}

SCRIPT_DIRECTORY="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

FORTRAN_AS_BASE_DIRECTORY=$(realpath "${SCRIPT_DIRECTORY}/..")

PARSERS_INJECT_NEEDLE="\/\/_FORTRAN_PARSERS_\/\/"
PARSER_INJECT_NEEDLE="FORTRAN_PARSER"
SOURCE_DIRECTORY="${FORTRAN_AS_BASE_DIRECTORY}/FortranAS/src"
TEMPLATE_DIRECTORY="${SOURCE_DIRECTORY}/org/fortranas/antlr4/templates"
OUTPUT_DIRECTORY="${SOURCE_DIRECTORY}/org/fortranas/antlr4/generated"
FORTRAN_PARSER_INSTANCE_OF_TEMPLATE="${TEMPLATE_DIRECTORY}/FortranParserInstanceOf.java.template"
FORTRAN_PARSER_LOADER_TEMPLATE_PATH="${TEMPLATE_DIRECTORY}/Antlr4ParserLoader.java.template"
FORTRAN_PARSER_LOADER_TEMPLATE="${TEMPLATE_DIRECTORY}/Antlr4ParserLoader.java.template"
FORTRAN_PARSERS_SEARCH_PATH="${FORTRAN_AS_BASE_DIRECTORY}/antlr4/generated"

GENERATED_FILE="${OUTPUT_DIRECTORY}/$(basename ${FORTRAN_PARSER_LOADER_TEMPLATE%%.template})"

mkdir -p "${OUTPUT_DIRECTORY}"

echo "Using fortran parsers search path: ${FORTRAN_PARSERS_SEARCH_PATH}"

 parsers=""
while IFS= read -r file; do
    echo "Fortran Parser: $file"
    file_name="$(basename "$file")"
    fortran_parser="${file_name%.*}"
    echo "  Found Fortran parser: $fortran_parser"
    parsers="${parsers}\n$(cat "${FORTRAN_PARSER_INSTANCE_OF_TEMPLATE}" | sed "s|${PARSER_INJECT_NEEDLE}|${fortran_parser}|g")"
done < <(find "$FORTRAN_PARSERS_SEARCH_PATH" -type f -regex '.*Fortran[0-9]+Parser.java')


echo -e "${parsers}"

echo "Injecting parsers into: ${GENERATED_FILE}"
insert_line=$(grep -n "${PARSERS_INJECT_NEEDLE}" "${FORTRAN_PARSER_LOADER_TEMPLATE}" | cut -d ":" -f1)
touch "${GENERATED_FILE}"
head -n "${insert_line}" "${FORTRAN_PARSER_LOADER_TEMPLATE}" > "${GENERATED_FILE}" 
echo -e "${parsers}" >> "${GENERATED_FILE}"
tail -n +$insert_line "${FORTRAN_PARSER_LOADER_TEMPLATE}" >> "${GENERATED_FILE}"
