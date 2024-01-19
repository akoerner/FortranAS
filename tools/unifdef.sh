#!/usr/bin/env bash
#
# File: unifdef.sh
#
# Description: This script recursively preprocesses Fortran source files in a specified directory by using the 'unifdef' tool.
#              It reads configuration parameters from the 'FortranAS.conf' file, including Fortran file extensions
#              and associated preprocessor symbols. For each matching file, it applies 'unifdef' with the specified
#              symbols, removes leading comments, and updates the file in-place.
#
# Usage: bash unifdef.sh
#
#
# Note: The script strips preprocessor definitions by defining all preprocessor symbols encountered in the files.
#       This is achieved by using 'unifdef' with the '-s' option to extract symbols, and then defining them using '-D'
#       flags when applying 'unifdef' to the files.
#

SCRIPT_DIRECTORY="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

if ! command -v unifdef &> /dev/null; then
    echo "ERROR: The command line tool 'unifdef' not found. Please install it and try again." >&2
    exit 1
fi

# Determine the absolute path of the source directory
directory_path="$(realpath "${SCRIPT_DIRECTORY}/../source")"

# Extract Fortran file extensions from configuration file
fortran_file_extensions="$(cat ${SCRIPT_DIRECTORY}/../FortranAS.conf | grep "fortran_file_extensions" | grep "=" | cut -d "=" -f2)"
fortran_file_extensions="${fortran_file_extensions#"${fortran_file_extensions%%[![:space:]]*}"}"
fortran_file_extensions="${fortran_file_extensions//,/\\n}"

# Read file extensions into an array
read -ra file_extensions <<< "$fortran_file_extensions"

# Iterate through each file extension and process matching files recursively
for file_extension in "${file_extensions[@]}"; do
    file_extension="$(echo ${file_extension} | sed 's|\\n||g')"

    # Find files with the specified extension in the source directory and its subdirectories
    find "$directory_path" -type f -iname "**$file_extension" -print0 | while IFS= read -r -d $'\0' file; do
        echo "Processing file: $file"

        # Perform 'unifdef' on the file to handle preprocessor symbols
        symbols=$(unifdef -s "$file" 2> /dev/null)
        symbol_flags=$(echo "$symbols" | sed 's/^/-D/g' | sed 's/ / -D/g' | tr '\n' ' ')
        unifdef -b $symbol_flags -t -m "$file" 2> /dev/null 

        # Remove leading comments from the processed file
        sed -i '/^[[:space:]]*#/ s/.*$/ /' "${file}"
    done
done

