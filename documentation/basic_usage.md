# Basic Usage

This section will provide a guide on the basic usage of **FortranAS**.

## Running **FortranAS** 

Grab the latest release: [https://github.com/akoerner/FortranAS/releases](https://github.com/akoerner/FortranAS/releases)

### Prerequisites
- Install JRE 19+
- Install GraphViz (for DOT file conversion to SVG and PNG)
- Install `unifdef` command line tool 
- Install `python3` for [Code Clone Analysis 🔗](../code_clone_analysis/README.md) 
- Install `meld` for clone pair diffing

## Usage
1. Install all prerequisites 
2. Copy FORTRAN source code to `./source` and optionally strip preprocessor directives using `tools/unifdef.sh`
3. Run FortranAS:
```bash
./fortranas --output-directory output \
            --input-source-code-directory source \
            --print-fortran-files \
            --parse-fortran-files \
            --calculate-code-clones
```
> **ℹ️INFO:**
> All **FortranAS** parsed and generated output files will be placed in `./output` 

4. Run the [Code Clone Analysis 🔗](../code_clone_analysis/README.md) scripts to 
generate and analyze code clone pairs.

## Running **FortranAS** With Docker
1. Follow the [Quickstart 🔗](documentation/quickstart.md) to setup **FortranAS**
2. Place FORTRAN source code in `./source` to parse and analyze it
- Optionally, strip preprocessor directives (See: [Non-standard Language Features](fortranas-command-line-arguments.md#non-standard-language-features)
)
3. Run the provided **FortranAS** docker context via make:
```bash
make run
```

> **ℹ️INFO:**
> All **FortranAS** parsed and generated output files will be placed in `./output` 

4. Run the [Code Clone Analysis 🔗](../code_clone_analysis/README.md) scripts to 
generate and analyze code clone pairs.
