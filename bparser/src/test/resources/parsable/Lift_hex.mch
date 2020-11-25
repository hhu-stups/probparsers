MACHINE Lift

ABSTRACT_VARIABLES  current_level

INVARIANT  current_level : 0..0x63

INITIALISATION current_level := 0x4

OPERATIONS

	inc = PRE current_level<99 THEN current_level := current_level + 1 END ;
	
	dec = PRE current_level /= 0x001 THEN current_level := current_level - 1 END 
	
END

