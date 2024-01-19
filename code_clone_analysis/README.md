# Code Clone Analysis
This directory contains helper scripts written in bash and python for code
clone analysis.

 - The python script `generate_clone_pairs.py` aggregates the clone pair data in
the `FortranAS.sqlite3` database. 
 - The bash script `explore_clones.py` automates diffing of the clone pairs with
   any diff tool such as meld.. 

Before running these tools a `FortranAS.sqlite3` database is required. Run
FortranAS `-c or --calculate-code-clones` flag before running these tools.

## Code Clone Pair Generation 
There is a python script provided in this directory called
`generate_clone_pairs.py` that reads the `FortranAS.sqlite3` database and
generates clone pair files in the output directory (`output/clones`). This 
script also generates a clone database called `clones.sqlite3` in the same 
output directory (`output/clones/clones.sqlite3`). 

To run clone pair generation first run `FortranAS` on your FORTRAN source code
to generate a `FortranAS.sqlite3` database. Once you have a database you can run
this script with:
```bash
python3 generate_clone_pairs.py
```

## Code Clone Pair Exploration
The output clone pair directory containing a reference and candidate can be
diffed using and diff tool such as [meld](https://meldmerge.org/).

Open the directories `output/clones/references` and `output/clones/candidates`
using a directory comparison using a diff tool.

Alternately, you can use the provided shell script to do an automated iterated
diff over all clone pairs in `output/clones` using the provided shell script (`explore_clones.sh`).
In the following example [meld](https://meldmerge.org/) is used

1. Set the `DIFFTOOL` environmental variable:
```bash
export DIFFTOOL=meld
```

2. Run the `explore_clones.sh` shell script:
```bash
bash explore_clones.sh
```

