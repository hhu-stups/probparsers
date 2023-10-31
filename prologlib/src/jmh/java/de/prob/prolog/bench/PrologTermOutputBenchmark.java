package de.prob.prolog.bench;

import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.PrologTermOutput;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.io.StringWriter;

@State(Scope.Benchmark)
@SuppressWarnings("unused")
public class PrologTermOutputBenchmark {

	@Benchmark
	public void string(Blackhole bh) {
		StringWriter sw = new StringWriter();
		IPrologTermOutput pto = new PrologTermOutput(sw, false);
		pto.printString("asdf foobarbaz");
		bh.consume(sw.toString());
	}

	@Benchmark
	public void atom_Escaped(Blackhole bh) {
		StringWriter sw = new StringWriter();
		IPrologTermOutput pto = new PrologTermOutput(sw, false);
		pto.printAtom("asdf foobarbaz");
		bh.consume(sw.toString());
	}

	@Benchmark
	public void atomOrNumber_Escaped(Blackhole bh) {
		StringWriter sw = new StringWriter();
		IPrologTermOutput pto = new PrologTermOutput(sw, false);
		pto.printAtomOrNumber("asdf foobarbaz");
		bh.consume(sw.toString());
	}

	@Benchmark
	public void atom(Blackhole bh) {
		StringWriter sw = new StringWriter();
		IPrologTermOutput pto = new PrologTermOutput(sw, false);
		pto.printAtom("asdfofoobarbaz");
		bh.consume(sw.toString());
	}

	@Benchmark
	public void atomOrNumber_Atom(Blackhole bh) {
		StringWriter sw = new StringWriter();
		IPrologTermOutput pto = new PrologTermOutput(sw, false);
		pto.printAtomOrNumber("asdfofoobarbaz");
		bh.consume(sw.toString());
	}

	@Benchmark
	public void atomOrNumber_Number(Blackhole bh) {
		StringWriter sw = new StringWriter();
		IPrologTermOutput pto = new PrologTermOutput(sw, false);
		pto.printAtomOrNumber("12345678901234");
		bh.consume(sw.toString());
	}

	@Benchmark
	public void number(Blackhole bh) {
		StringWriter sw = new StringWriter();
		IPrologTermOutput pto = new PrologTermOutput(sw, false);
		pto.printNumber(12345678901234L);
		bh.consume(sw.toString());
	}
}
