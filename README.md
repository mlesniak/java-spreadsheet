# Overview

This is a small non-graphical (non-serious) spreadsheet implementation, which works on CSV files.
It supports cell dependencies, formulas which consider precedence rules and brackets, and can catch
circular references.

Formula evaluation is done via Shunting Yard algorithm, which converts infix notation to postfix and
is then evaluated by a simple stack machine.

## Example

The following is an example of a CSV file which can be loaded by the program. Rows are described by
letters and columns by numbers.

| A     | B      | C   | D          | E  |
|-------|--------|-----|------------|----|
| 34    | =A2    | ""  |            |    |
| =B2*2 |        | 45  | =3+A2*2    | "" |
|       | =C4    | =D4 | 55         |    |
|       | string | =A3 | =(C3+A2)*3 |    |