package de.be4.eventbalg.parser;

import org.junit.Test;

import de.be4.eventbalg.core.parser.BException;
import de.be4.eventbalg.core.parser.EventBParser;

public class AlgorithmTest {

	@Test
	public void test() throws BException {
		new EventBParser().parse("machine m0 algorithm while: 1=1 do assert: 1=3 end; assert: 1=1; assert: 1=2 end end");
	}

	@Test
	public void test2() throws BException {
		new EventBParser().parse("machine m0 algorithm while: u /=0 do if: u < v then @act u:=v; @act2 v:=u else @act4 r:=x end; @act3 u := u - v end end end");
	}

	@Test
	public void test3() throws BException {
		new EventBParser().parse("machine m0 algorithm while: u /=0 do if: u < v then @act u:=v; @act2 v:=u else @act4 r:=x end; @act3 u := u - v end end end");
	}

	@Test
	public void nestedWhiles() throws BException {
		new EventBParser().parse("machine m0 algorithm while: a do while: b do @act u:=v end; while: c do @act2 u:=r end end end end");
	}

	@Test
	public void nestedWhilesWithVariant() throws BException {
		new EventBParser().parse("machine m0 algorithm while: a variant x+y do while: b variant b do @act u:=v end; while: c do @act2 u:=r end end end end");
	}

	@Test
	public void nestedWhilesWithVariantAndInvariant() throws BException {
		new EventBParser().parse("machine m0 algorithm while: a invariant x+y=0 variant x+y   do while: b invariant x+y=0 variant b  do @act u:=v end; while: c do @act2 u:=r end end end end");
	}

	@Test
	public void nestedWhilesWithInvariant() throws BException {
		new EventBParser().parse("machine m0 algorithm while: a invariant x+y=0 do while: b invariant x+y=0 do @act u:=v end; while: c do @act2 u:=r end end end end");
	}

	@Test
	public void nestedIfs() throws BException {
		new EventBParser().parse("machine m0 algorithm if: a then if: b then @act u:=v end else @act4 r := x end; while: c do @act2 u:=r end end end");
	}

	@Test
	public void fullEuclid() throws BException {
		new EventBParser().parse("machine euclid\n  sees definitions limits\nvariables u v\ninvariants\n  @inv u : 0..k\n  @inv2 v : 0..k\n\nevents\n  event INITIALISATION\n    then\n      @act u:=m\n      @act2 v:=n\n  end\n\n  algorithm\n    while: u /= 0 \n    do\n      if: u < v then\n        @u u := v ;\n        @v v := u\n      end ;\n      @uu u := u - v\n    end ;\n    assert: u|->m|->n : IsGCD\n  end\n\nend");
	}

	@Test
	public void withVariant() throws BException {
		new EventBParser().parse("machine euclid\n  sees definitions limits\nvariables u v\ninvariants\n  @inv u : 0..k\n  @inv2 v : 0..k\n  @loop GCD[{m↦n}] = GCD[{u↦v}]\n\nevents\n  event INITIALISATION\n    then\n      @act u:=m\n      @act2 v:=n\n  end\n\nalgorithm\n  while: u /= 0 \n  	variant u + v\n  do\n    if: u < v then\n      @v v := v - u \n    else\n      @u u := u - v \n    end \n  end ;\n  assert: m↦n↦v ∈ GCD\nend\n\nend\n");
	}

	@Test
	public void sixStatements() throws BException {
		new EventBParser().parse("machine m0 algorithm @x x:=1 ; @y x:=1 ; @z z:=1 ; @f f:=1 ; @g g:=1 ; @h h:=1 end end");
	}

	@Test
	public void callAProcedure() throws BException {
		new EventBParser().parse("machine m0 algorithm e,q := dequeue(q) end end");
	}

	@Test
	public void callAProcedureNoArgs() throws BException {
		new EventBParser().parse("machine m0 algorithm e,q := dequeue() end end");
	}
}
