# Compiler
## Overview
This is the repository for the assignment of COT5556 Programming Language Principles.

The project is a compiler based on Java, using Maven as the package manage tool.

Detailed documentations are in the `docs` folder.

## Project structure
* `src`: source code and tests
* `docs`: documentations
* `pom.xml`: configuration file for Maven.

## documentations

### nested structure and variables in the enclosing scope

```
prog:
    VAR a;
    PROCEDURE p:
        PROCEDURE q:
            a = 42
LDC 42
ALOAD 0
GETFIELD edu/ufl/cise/plpfa22/prog$p$q.this$1 : Ledu/ufl/cise/plpfa22/prog$p
GETFIELD edu/ufl/cise/plpfa22/prog$p.this$0 : Ledu/ufl/cise/plpfa22/prog
SWAP
PUTFIELD edu/ufl/cise/plpfa22/prog.a : I

prog:
    PROCEDURE p:
        CALL q;
    PROCEDURE q:
        CALL p;
```