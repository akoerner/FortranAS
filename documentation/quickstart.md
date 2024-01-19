# Quickstart
This guide will demonstrate how to quickly get **FortranAS** up and running.

## Prerequisites
This project includes docker context to build and run **FortranAS**.
In order to build **FortranAS** you must have GNU Make and Docker installed.
Installation of theses tools for your system is out of scope for this guide
although it is recommended you follow the official 
[Docker Installation 🔗](https://docs.docker.com/engine/install/) guide to
install docker on your system.

You can also build and run **FortranAS** locally via maven.
For local builds you need `JDK 19+` and `Maven` installed. Local builds are out
of scope for this guide.

- Install GraphViz (for DOT conversion)
- Install `unifdef` command line tool
- Install `python3`
- Install `meld` for clone pair diffing


> **ℹ️INFO:**
> **FortranAS** can only be built on Linux with Docker and GNU Make installed.  
> For local builds `JDK 19` or better is required.  

## Building **FortranAS**
1. Clone the **FortranAS** repository:
```bash
git clone --recursive --jobs=$(nproc) https://github.com/akoerner/FortranAS.git
```
> ⚠️ **WARNING:**
> **FortranAS** requires the [Antlr4 🔗](https://github.com/antlr/antlr4) and
> [grammars-v4 🔗](https://github.com/antlr/grammars-v4/tree/master/fortran)
> submodules. If submodules are not initialized and updated then the build of
> **FortranAS** will fail.

2. Build **FortranAS**:
```bash
cd FortranAS
make build
```

## Running The **FortranAS** Demo With Docker
After building **FortranAS** the provided demo make target can be run with:
```bash
make run_demo
```
The demo will parse all FORTRAN source code in 
[fortran_code_samples](../fortran_code_samples) directory with all
output being placed in `./output`. The demo will also generate code pairs using
the [code_clone_analysis/generate_clone_pairs.py](code_clone_analysis/generate_clone_pairs.py)
python script.

