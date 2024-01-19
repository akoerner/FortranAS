#!/bin/bash

# This script tests if UTF-8 is properly supported by the current interactive
# session.

echo -e "Testing UTF-8 encoding\n\n日本語も大丈夫ですか？" > utf8_test.txt

file_encoding=$(file -bi utf8_test.txt | awk -F "=" '{print $2}')
utf8_representation=$(cat utf8_test.txt)

echo "File Encoding: $file_encoding"
echo "UTF-8 Representation: ${utf8_representation}"

if [ "$file_encoding" == "utf-8" ]; then
    echo "UTF-8 encoding test passed!"
else
    echo "UTF-8 encoding test failed!"
fi

rm utf8_test.txt

