MACHINE ParserDefBug
 DEFINITIONS
 D(y) == f
 
CONSTANTS f,g
	PROPERTIES
 f = pred &
	
             g = %x.(x:1..10 | (D("hello")(x)))
	
ASSERTIONS
	g(1)=0
 END