MACHINE TestWhileDEF
DEFINITIONS
  AA==1;
  MyWhile == WHILE x > 0 DO x:=x-1 INVARIANT x:NATURAL VARIANT x END;
  BB == "WHILE x > 0 DO x:=x-1 INVARIANT x:NATURAL VARIANT x END"
SETS
 ID={aa,bb}

VARIABLES xx
INVARIANT
 xx:ID
INITIALISATION xx:=aa
OPERATIONS
  res <-- Test = VAR x IN BEGIN
            x:=10;
            WHILE x > 0 DO x:=x-1 INVARIANT x:NATURAL VARIANT x END;
           res := x
         END
    END
END
