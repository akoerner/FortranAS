# FortranAS
**FortranAS** is an [Antlr4 🔗](https://github.com/antlr/antlr4) powered 
**FORTRAN** parsing and code clone detection tool. **FortranAS** leverages 
[grammars-v4 🔗](https://github.com/antlr/grammars-v4/tree/master/fortran) to 
generate parsers and lexers and supports any **FORTRAN** version with
available corresponding grammars.

## FortranAS Key Features
- Docker build and runtime environment
- Docker build context for Antlr4 and Grammars-v4 (See: [Antlr4 Build Context](antlr4/README.md))
- Extensible **FORTRAN** Grammars via 
[grammars-v4 🔗](https://github.com/antlr/grammars-v4/tree/master/fortran).
**FortranAS** supports any **FORTRAN** version with corresponding grammars.
- **FORTRAN** source representation as Antler4 parse tree
- Conversion of concrete Antlr4 parse tree to generic tree data structure
- Translation of parse tree to abstract syntax tree representation
- Serialization of all data structures(parse trees, abstract syntax trees, 
Antlr4 tokens) to: JSON, GraphViz DOT (for tree visualization), text file (tree
terse string)
- Code clone detection with abstract syntax trees approach using natural language processing metrics such as BLEU Score and Jaro-Winkler-Similarity

## TOC
1. [Quick Start](documentation/quickstart.md)
2. [Basic Usage](documentation/basic_usage.md)
3. [FortranAS Artifacts](documentation/artifacts.md)
4. [FortranAS Config File](documentation/fortranas-config-file.md)
5. [FortranAS Command Line Arguments](documentation/fortranas-command-line-arguments.md)
6. [Troubleshooting](documentation/troubleshooting.md)
7. [Antlr4 Build Context](antlr4/README.md)

