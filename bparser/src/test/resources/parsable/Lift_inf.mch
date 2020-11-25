MACHINE Lift

ABSTRACT_VARIABLES  current_level

INVARIANT  current_level : 0..99 

INITIALISATION current_level := 4

OPERATIONS

	inc = PRE current_level<99 THEN current_level := current_level + 1 END ;
	
	dec = PRE current_level /= 1 THEN current_level := current_level - 1 END 
	
END

