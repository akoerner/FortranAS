#!/usr/bin/env bash

echoerr (){ printf "%s" "$@" >&2;}
exiterr (){ printf "%s\n" "$@" >&2; exit 1;}

SCRIPT_DIRECTORY="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

if [ -z "${DIFFTOOL}" ]; then
    exiterr "ERROR: DIFFTOOL is not set. Please set the DIFFTOOL environment variable."
fi

clone_pair_directory="${SCRIPT_DIRECTORY}/../output/clones"
references_directory="${clone_pair_directory}/references"
candidates_directory="${clone_pair_directory}/candidates"
files=($(find "$references_directory" -type f -exec basename {} \; | sort -r))



for file in "${files[@]}"; do
    $DIFFTOOL "${references_directory}/${file}" "${candidates_directory}/${file}"
done


