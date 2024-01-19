# Antlr4 docker context
This directory contains a `Dockerfile` and `Makefile` to compile/build Antlr4 
grammars for FORTRAN. This is invoked automatically by the **FortranAS** build
process and does not need to be manually invoked.

## Usage
Assuming you have make and docker installed run:
```bash
make
```

## Artifacts
Once build all artifacts will be placed in `antlr4/generated` 

## Clean target 
To clean all build artifacts you can run the provided make target:
```bash
make clean
```
