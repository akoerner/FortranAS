# Troubleshooting
This section offers some known problems and solutions.


## Problem: Insufficient free RAM
If you are receiving a message similar to the following you can free up some
cache and try again:
```text
Insufficient free RAM: 7 GB free, Minimum required: 8 GB. Free up some ram and try again.
```


To free up cache/RAM run the following:
```bash
sudo sh -c 'echo 1 > /proc/sys/vm/drop_caches'
```



## Problem: Antlr4 reporting partial parsing error
Antlr4 may report a parsing error similar to:
```
 Partial parsing error, Antlr4 reported parsing error, for more information view the antlr4 parsing log file ...
```

Review the corresponding `antlr4_parse_tree.log` file in the output directory to
troubleshoot Antlr4 parsing errors.

Every FORTRAN source file that is parsed will have a corresponding
`.antlr4_parse_tree.log` in the output files with all Antlr4 parsing output.
Review the [FortranAS Artifacts](artifacts.md) for more information on this log
file.

## Problem: Code clone calculations will not finish before the heat death of the universe
Code clone prediciton calculations are <sup>O(n<sup>2</sup>)</sup>
 with respect to the number of
`subtrees` in the FortranAS database. To reduce the number of subtrees and
subsequent the number of code clone calculations and the subtrees can be
selectively inserted into the database bounded by subtree size and subtree depth.
Refer to the [Subtree Bounding](fortranas-command-line-arguments.md#subtree-bounding)
guide for more information.

