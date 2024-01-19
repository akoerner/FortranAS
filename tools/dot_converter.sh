#!/usr/bin/env bash
#
# dot-converter.sh
#
# This script recursively traverses the 'output' directory for files with the .dot extension.
# It uses the Graphviz 'dot' command to convert them first to SVG and then the
# 'convert' command to convert the SVG files to PNG. The converted files are 
# saved in the same directory with the same filename but different extensions (.svg and .png).
#
# Usage: bash dot-to-png-converter.sh

command_exists() {
  command -v "$1" >/dev/null 2>&1
}

if [[ $(command_exists convert) ]]; then
  echo "ERROR: The ImagMagick 'convert' command not found. Please install ImageMagick and try again." >&2
  exit 1
fi

if [[ $(command_exists dot) ]]; then
  echo "ERROR: GraphViz 'dot'  command not found. Please install Graphviz and try again." >&2
  exit 1
fi

directory="output"

if [ ! -d "$directory" ]; then
  echo "ERROR: The 'output' directory does not exist." >&2
  exit 1
fi

while IFS= read -r -d '' dot_file; do
  svg_file="${dot_file%.dot}.svg"
  png_file="${dot_file%.dot}.png"

  # Convert dot file to SVG
  dot -Tsvg -o"$svg_file" "$dot_file"

  # Convert the SVG to PNG
  convert "$svg_file" "$png_file"

  echo "Converted: $dot_file to $png_file"
done < <(find "$directory" -type f -name "*.dot" -print0)

