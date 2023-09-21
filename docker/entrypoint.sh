#!/usr/bin/env bash

set -euo pipefail

cd /app/build/
./fortranas -L Fortran90Lexer -o /app/output -i /app/fortran_code_samples
