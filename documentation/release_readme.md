# FortranAS
**FortranAS** is an [Antlr4 🔗](https://github.com/antlr/antlr4) powered 
**FORTRAN** parsing and code clone detection tool. **FortranAS** leverages 
[grammars-v4 🔗](https://github.com/antlr/grammars-v4/tree/master/fortran) to 
generate parsers and lexers and supports any **FORTRAN** version with
available corresponding grammars.

## Prerequisites
- Install JRE 19+
- Install GraphViz (for DOT conversion)
- Install `unifdef` command line tool
- Install `python3`
- Install `meld` for clone pair diffing

## Usage
1. Copy FORTRAN source code to `./source` and optionally strip preprocessor directives using `tools/unifdef.sh`
2. Run FortranAS:
```bash
./fortranas --output-directory output \
            --input-source-code-directory source \
            --print-fortran-files \
            --parse-fortran-files \
            --calculate-code-clones
```
3. Generate clone pairs:
```bash
python3 code_clone_analysis/generate_clone_pairs.py
```
3. Explore clone pairs using preferred diffing tool by diffing
   `./output/clones/references` and `./output/clones/candidates`.
   Optionally, use the clone explorer script provided by **FortranAS**:
```bash
export DIFFTOOL=meld
bash tools/explore
```

## Non-standard Language Features
The Antlr4 grammars cannot parse FORTRAN source code that uses non-standard language
features such as preprocessor directives. In order to process
these files with **FortranAS** the preprocessor directives must be removed.
There is no perfect way to do this. **FortranAS** provides a shell script that
uses the command line tool [unifdef](https://dotat.at/prog/unifdef/) in the
tools directory called `unifdef.sh`. This shell script will strip all
preprocessor directives from the FORTRAN source code in the `./source` directory.
To run it place FORTRAN `./source` and run the provide script:
```bash
bash tools/unifdef.sh
```


