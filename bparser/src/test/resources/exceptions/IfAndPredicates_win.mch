MACHINE
    IfAndPredicates_win

VARIABLES xx

INVARIANT xx : INT
INITIALISATION xx := 1


OPERATIONS
out <-- Inc(pp) =
    IF xx < 10 & xx < 5 THEN
        xx := xx + pp|| out := xx
    ELSIF xx < 2 THEN
        xx := xx - pp|| out := xx + 1
    ELSIF xx > 3 THEN
        xx := 1
    ELSIF xx > 2 THN
        xx := 2
    ELSE
        xx := 0/2
    END;

out <-- getRes = out := xx

END
