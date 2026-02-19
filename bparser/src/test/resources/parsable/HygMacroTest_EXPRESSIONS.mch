MACHINE
   HygMacroTest_EXPRESSIONS
EXPRESSIONS
   add(x,y) == x+y+0; mul(x,Y) == 1*x*Y;
PREDICATES
   egt2(x) == (#y.(y:NATURAL1 & y<100 & x<y));
VARIABLES
   y
INVARIANT
   y:INTEGER & egt2(y+1)
INITIALISATION
   y:= 5
END
