#!/usr/bin/env bash

set -euo pipefail

echoerr (){ printf "%s" "$@" >&2;}
exiterr (){ printf "%s\n" "$@" >&2; exit 1;}

SCRIPT_DIRECTORY="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

if [ $# -ne 1 ]; then
    echo "Usage: $0 <requirements_file>"
    exit 1
fi

requirements_file="$1"
output_file="${requirements_file}.save"

rm -rf "${output_file}"
touch "${output_file}"

while IFS= read -r line; do
    if [[ "$line" =~ ^[[:space:]]*$ || "$line" =~ ^# ]]; then
        echo "${line}" >> "${output_file}"
        continue
    fi

    package_name=$(echo "$line" | xargs)

    package_version=$(apt-cache show "$package_name" 2>/dev/null | grep -oP "Version: \K.*"  | sed -n '1p' || echo "")

    if [ -n "$package_version" ]; then
        echo "Package: $package_name, Version: $package_version"
        echo "${package_name}=${package_version}" >> "${output_file}"
    else
        echo "Package: $package_name, Version not found"
        echo "#${package_name}=NOT INSTALLED" >> "${output_file}"
    fi
done < "$requirements_file"
chmod +w "${output_file}"
