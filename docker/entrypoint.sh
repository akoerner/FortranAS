#!/usr/bin/env bash

set -euo pipefail

cd /tmp/FortranAS
./build/fortranas --output-directory /tmp/FortranAS/output \
                  --input-source-code-directory /tmp/FortranAS/source \
                  --print-fortran-files \
                  --parse-fortran-files \
                  --calculate-code-clones
