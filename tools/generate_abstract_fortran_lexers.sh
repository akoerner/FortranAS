#!/usr/bin/env bash

set -euo pipefail

echoerr (){ printf "%s" "$@" >&2;}
exiterr (){ printf "%s\n" "$@" >&2; exit 1;}

SCRIPT_DIRECTORY="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

FORTRAN_AS_BASE_DIRECTORY=$(realpath "${SCRIPT_DIRECTORY}/..")
LEXER_INJECT_NEEDLE="_LEXER_"
SOURCE_DIRECTORY="${FORTRAN_AS_BASE_DIRECTORY}/FortranAS/src"
TEMPLATE_DIRECTORY="${SOURCE_DIRECTORY}/org/fortranas/antlr4/templates"
OUTPUT_DIRECTORY="${SOURCE_DIRECTORY}/org/fortranas/antlr4/generated"
ABSTRACT_FORTRAN_LEXER_TEMPLATE="${TEMPLATE_DIRECTORY}/AbstractFortranLexer.java.template"
FORTRAN_LEXERS_SEARCH_PATH="${FORTRAN_AS_BASE_DIRECTORY}/antlr4/generated"

mkdir -p "${OUTPUT_DIRECTORY}"

echo "Using Fortran lexers search path: ${FORTRAN_LEXERS_SEARCH_PATH}"

while IFS= read -r file; do
    echo "Fortran Lexer: $file"
    file_name="$(basename "$file")"
    fortran_lexer="${file_name%.*}"
    echo "  Found Fortran lexer: $fortran_lexer"
    generated_file="${OUTPUT_DIRECTORY}/Abstract${fortran_lexer}.java"
    rm -rf "${generated_file}"
    cp "${ABSTRACT_FORTRAN_LEXER_TEMPLATE}" "${generated_file}"
    sed -i "s|${LEXER_INJECT_NEEDLE}|${fortran_lexer}|g" "${generated_file}"
    echo "  Generated abstract fortran lexer: ${generated_file}"
done < <(find "$FORTRAN_LEXERS_SEARCH_PATH" -type f -regex '.*Fortran[0-9]+Lexer.java')
