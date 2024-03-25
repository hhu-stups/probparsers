package de.be4.classicalb.core.parser.prettyprinter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.util.Utils;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PrettyMachinePrinterTest {

	@Test
	public void testPrettyPrint() {
		final String testMachine = "MACHINE Test\n" +
			"VARIABLES x, y\n" +
			"INVARIANT x:INTEGER & y:INTEGER\n" +
			"INITIALISATION x := 1 || y := 2\n" +
			"OPERATIONS\n" +
			"foo = skip;\n" +
			"bar = skip\n" +
			"END";
		final String result1 = Helpers.getPrettyPrint(testMachine);

		assertFalse(result1.isEmpty());
		assertEquals(testMachine, result1);
	}

	@Test
	public void testPrettyPrint_Complicated_Indent() throws BCompoundException {
		final String testMachine = "MACHINE NeedhamSchroeder\n" +
			"DEFINITIONS\n" +
			"SET_PREF_MAX_OPERATIONS == 100;\n" +
			"SET_PREF_OPERATION_REUSE == \"full\";\n" +
			"SET_PREF_COMPRESSION == \"true\";\n" +
			"GENERATED_VARS == {(initiator,Na),(responder,Nb)};\n" +
			"GOAL == not(!honest_tid.(honest_tid:dom(threads) & thread_actions(honest_tid)=[] & Adversary/:ran(threads(honest_tid)'sub) => thread_memory(honest_tid)[GENERATED_VARS[{threads(honest_tid)'role}]/\\{Na,Nb}]/\\adversary_knowledge={}));\n" +
			"SETS STATE={Create,Run}; ROLE={initiator,responder}; ACTION={generate_2,send_1,send_3,receive_3,receive_2,send_2,receive_1,generate_1}; FRESH={Na,Nb}; AGENT={Adversary,Alice,Bob}\n" +
			"FREETYPES\n" +
			"TERM = PublicKey(AGENT), PrivateKey(AGENT), Enc(struct(key:TERM,term:TERM)), Seq(seq(TERM)), Fresh(NATURAL1)\n" +
			"CONSTANTS MaxThreads, Protocol, InverseRole, InverseKey\n" +
			"PROPERTIES\n" +
			"MaxThreads=2 & Protocol={(responder,[receive_1,generate_2,send_2,receive_3]),(initiator,[generate_1,send_1,receive_2,send_3])} & InverseRole={(responder,initiator),(initiator,responder)} & InverseKey=%k.(k:ran(PublicKey)|PrivateKey((PublicKey~)(k)))\\/%k.(k:ran(PrivateKey)|PublicKey((PrivateKey~)(k)))\n" +
			"VARIABLES state, next_fresh, threads, thread_actions, thread_memory, adversary_knowledge\n" +
			"INVARIANT state:STATE & next_fresh:NATURAL1 & threads:seq(struct(role:dom(Protocol),sub:dom(Protocol)>->AGENT)) & thread_actions:dom(threads)-->seq(ACTION) & thread_memory:dom(threads)-->(FRESH+->TERM) & adversary_knowledge<:TERM\n" +
			"INITIALISATION state := Create || next_fresh := 1 || threads := [] || thread_actions := [] || thread_memory := [] || adversary_knowledge := PublicKey[AGENT]\\/{PrivateKey(Adversary)}\n" +
			"OPERATIONS\n" +
			"create(role, sub) = PRE state=Create & size(threads)<MaxThreads & role:dom(Protocol) & sub:dom(Protocol)>->AGENT THEN threads := threads<-rec(role:role,sub:sub) || thread_actions := thread_actions<-Protocol(role) || thread_memory := thread_memory<-{} END ;\n" +
			"start = PRE state=Create THEN state := Run END ;\n" +
			"unpack(term) = PRE state=Run & term:adversary_knowledge & term:ran(Seq) & ran((Seq~)(term))\\adversary_knowledge/={} THEN adversary_knowledge := adversary_knowledge\\/ran((Seq~)(term)) END ;\n" +
			"decrypt(term) = PRE state=Run & term:adversary_knowledge & term:ran(Enc) & LET key BE key=(Enc~)(term)'key IN key:dom(InverseKey) & InverseKey(key):adversary_knowledge END & (Enc~)(term)'term/:adversary_knowledge THEN adversary_knowledge := adversary_knowledge\\/{(Enc~)(term)'term} END ;\n" +
			"generate_1(tid) = PRE state=Run & tid:dom(threads) & {generate_1}=thread_actions(tid)[{1}] THEN thread_actions(tid) := tail(thread_actions(tid)) || next_fresh := next_fresh+1 || thread_memory(tid)(Na) := Fresh(next_fresh) END ;\n" +
			"send_1(tid, term) = PRE state=Run & tid:dom(threads) & {send_1}=thread_actions(tid)[{1}] & term:ran(Enc) & (Enc~)(term)'key=PublicKey(threads(tid)'sub(responder)) & (Enc~)(term)'term:ran(Seq) & size((Seq~)((Enc~)(term)'term))=2 & (Seq~)((Enc~)(term)'term)(1)=PublicKey(threads(tid)'sub(initiator)) & {(Seq~)((Enc~)(term)'term)(2)}=thread_memory(tid)[{Na}] THEN thread_actions(tid) := tail(thread_actions(tid)) || adversary_knowledge := adversary_knowledge\\/{term} END ;\n" +
			"receive_2(tid, term) = PRE state=Run & tid:dom(threads) & {receive_2}=thread_actions(tid)[{1}] & term:ran(Enc) & (Enc~)(term)'key=PublicKey(threads(tid)'sub(initiator)) & (Enc~)(term)'term:ran(Seq) & size((Seq~)((Enc~)(term)'term))=2 & (Seq~)((Enc~)(term)'term)(1)=thread_memory(tid)(Na) & (Seq~)((Enc~)(term)'term)(2):ran(Fresh) & (term:adversary_knowledge or ((Enc~)(term)'key:adversary_knowledge & ((Enc~)(term)'term:adversary_knowledge or ((Seq~)((Enc~)(term)'term)(1):adversary_knowledge & ((Seq~)((Enc~)(term)'term)(2):adversary_knowledge or (Seq~)((Enc~)(term)'term)(2)=Fresh(next_fresh+0)))))) THEN thread_actions(tid) := tail(thread_actions(tid)) || next_fresh := next_fresh+1 || thread_memory(tid)(Nb) := (Seq~)((Enc~)(term)'term)(2) END ;\n" +
			"send_3(tid, term) = PRE state=Run & tid:dom(threads) & {send_3}=thread_actions(tid)[{1}] & term:ran(Enc) & (Enc~)(term)'key=PublicKey(threads(tid)'sub(responder)) & {(Enc~)(term)'term}=thread_memory(tid)[{Nb}] THEN thread_actions(tid) := tail(thread_actions(tid)) || adversary_knowledge := adversary_knowledge\\/{term} END ;\n" +
			"receive_1(tid, term) = PRE state=Run & tid:dom(threads) & {receive_1}=thread_actions(tid)[{1}] & term:ran(Enc) & (Enc~)(term)'key=PublicKey(threads(tid)'sub(responder)) & (Enc~)(term)'term:ran(Seq) & size((Seq~)((Enc~)(term)'term))=2 & (Seq~)((Enc~)(term)'term)(1)=PublicKey(threads(tid)'sub(initiator)) & (Seq~)((Enc~)(term)'term)(2):ran(Fresh) & (term:adversary_knowledge or ((Enc~)(term)'key:adversary_knowledge & ((Enc~)(term)'term:adversary_knowledge or ((Seq~)((Enc~)(term)'term)(1):adversary_knowledge & ((Seq~)((Enc~)(term)'term)(2):adversary_knowledge or (Seq~)((Enc~)(term)'term)(2)=Fresh(next_fresh+0)))))) THEN thread_actions(tid) := tail(thread_actions(tid)) || next_fresh := next_fresh+1 || thread_memory(tid)(Na) := (Seq~)((Enc~)(term)'term)(2) END ;\n" +
			"generate_2(tid) = PRE state=Run & tid:dom(threads) & {generate_2}=thread_actions(tid)[{1}] THEN thread_actions(tid) := tail(thread_actions(tid)) || next_fresh := next_fresh+1 || thread_memory(tid)(Nb) := Fresh(next_fresh) END ;\n" +
			"send_2(tid, term) = PRE state=Run & tid:dom(threads) & {send_2}=thread_actions(tid)[{1}] & term:ran(Enc) & (Enc~)(term)'key=PublicKey(threads(tid)'sub(initiator)) & (Enc~)(term)'term:ran(Seq) & size((Seq~)((Enc~)(term)'term))=2 & {(Seq~)((Enc~)(term)'term)(1)}=thread_memory(tid)[{Na}] & {(Seq~)((Enc~)(term)'term)(2)}=thread_memory(tid)[{Nb}] THEN thread_actions(tid) := tail(thread_actions(tid)) || adversary_knowledge := adversary_knowledge\\/{term} END ;\n" +
			"receive_3(tid, term) = PRE state=Run & tid:dom(threads) & {receive_3}=thread_actions(tid)[{1}] & term:ran(Enc) & (Enc~)(term)'key=PublicKey(threads(tid)'sub(responder)) & (Enc~)(term)'term=thread_memory(tid)(Nb) & (term:adversary_knowledge or ((Enc~)(term)'key:adversary_knowledge & (Enc~)(term)'term:adversary_knowledge)) THEN thread_actions(tid) := tail(thread_actions(tid)) END \n" +
			"END";
		final String result1 = Helpers.getPrettyPrintWithIndentation(testMachine);
		final String result2 = Helpers.getPrettyPrintWithIndentation(result1);

		assertFalse(result1.isEmpty());
		assertFalse(result2.isEmpty());
		assertEquals(result1, result2);
		assertEquals(Helpers.getMachineAsPrologTerm(testMachine), Helpers.getMachineAsPrologTerm(result1));
		assertEquals(Helpers.getMachineAsPrologTerm(testMachine), Helpers.getMachineAsPrologTerm(result2));
		assertEquals(Helpers.getMachineAsPrologTerm(result1), Helpers.getMachineAsPrologTerm(result2));
	}

	@Test
	public void testPrettyPrint_Indent() {
		final String testMachine = "MACHINE Test\n" +
			"    DEFINITIONS\n" +
			"        FOO == 1=1;\n" +
			"        BAR == 1/=1\n" +
			"    VARIABLES\n" +
			"        x,\n" +
			"        y\n" +
			"    INVARIANT\n" +
			"        x:INTEGER &\n" +
			"        y:INTEGER\n" +
			"    INITIALISATION\n" +
			"        x := 1 ||\n" +
			"        y := 2\n" +
			"    OPERATIONS\n" +
			"        foo =\n" +
			"            skip;\n" +
			"        bar =\n" +
			"            skip\n" +
			"END";
		final String result1 = Helpers.getPrettyPrintWithIndentation(testMachine);

		assertFalse(result1.isEmpty());
		assertEquals(testMachine, result1);
	}

	@Test
	public void testPrettyPrint2() throws IOException, URISyntaxException {
		final URI uri = this.getClass()
			.getResource("/prettyprinter/PrettyPrinter.mch")
			.toURI();
		final File file = new File(uri);
		final String testMachine = Utils.readFile(file);
		final String result1 = Helpers.getPrettyPrint(testMachine);
		final String result2 = Helpers.getPrettyPrint(result1);

		assertFalse(result1.isEmpty());
		assertFalse(result2.isEmpty());
		assertEquals(result1, result2);
	}

	@Test
	public void testPrettyPrint2_Indent() throws IOException, URISyntaxException {
		final URI uri = this.getClass()
			.getResource("/prettyprinter/PrettyPrinter.mch")
			.toURI();
		final File file = new File(uri);
		final String testMachine = Utils.readFile(file);
		final String result1 = Helpers.getPrettyPrintWithIndentation(testMachine);
		final String result2 = Helpers.getPrettyPrintWithIndentation(result1);

		assertFalse(result1.isEmpty());
		assertFalse(result2.isEmpty());
		assertEquals(result1, result2);
	}

	@Test
	public void testPrettyPrint3() throws IOException, URISyntaxException {
		final URI uri = this.getClass()
			.getResource("/prettyprinter/PrettyPrinter2.mch")
			.toURI();
		final File file = new File(uri);
		final String testMachine = Utils.readFile(file);
		final String result1 = Helpers.getPrettyPrint(testMachine);
		final String result2 = Helpers.getPrettyPrint(result1);

		assertFalse(result1.isEmpty());
		assertFalse(result2.isEmpty());
		assertEquals(result1, result2);
	}

	@Test
	public void testPrettyPrint3_Indent() throws IOException, URISyntaxException {
		final URI uri = this.getClass()
			.getResource("/prettyprinter/PrettyPrinter2.mch")
			.toURI();
		final File file = new File(uri);
		final String testMachine = Utils.readFile(file);
		final String result1 = Helpers.getPrettyPrintWithIndentation(testMachine);
		final String result2 = Helpers.getPrettyPrintWithIndentation(result1);

		assertFalse(result1.isEmpty());
		assertFalse(result2.isEmpty());
		assertEquals(result1, result2);
	}

	@Test
	public void testPrettyPrint4() {
		final String testMachine = "MACHINE Test\n" +
			"DEFINITIONS\nCHOOSE(X) == \"a member of X\";\nEXTERNAL_FUNCTION_CHOOSE(T) == POW(T)-->T\n" +
			"VARIABLES x, y, z\n" +
			"INVARIANT 1=1\n" +
			"INITIALISATION skip\n" +
			"OPERATIONS\nfoo = skip;\nbar = skip\n" +
			"END";
		final String result1 = Helpers.getPrettyPrint(testMachine);

		assertFalse(result1.isEmpty());
		assertEquals(testMachine, result1);
	}

	@Test
	public void testPrettyPrintGenerated() {
		String testMachine = "/*@generated*/\nMACHINE Test\nEND";
		assertEquals(testMachine, Helpers.getPrettyPrint("/*@generated*/ MACHINE Test\nEND"));
	}

	@Test
	public void testPrettyPrintPackage() {
		String testMachine = "/*@package one.two */\nMACHINE Test\nEND";
		assertEquals(testMachine, Helpers.getPrettyPrint(testMachine));
	}

	@Test
	public void testPrettyPrintPackageString() {
		String testMachine = "/*@package \"one two\" */\nMACHINE Test\nEND";
		assertEquals(testMachine, Helpers.getPrettyPrint(testMachine));
	}

	@Test
	public void testPrettyPrintPackageImport() {
		String testMachine = "/*@package one.two */\n/*@import-package one.three */\nMACHINE Test\nEND";
		assertEquals(testMachine, Helpers.getPrettyPrint(testMachine));
	}

	@Test
	public void testPrettyPrintPackageImports() {
		String testMachine = "/*@package one.two */\n/*@import-package one.three */\n/*@import-package \"one.two three\" */\nMACHINE Test\nEND";
		assertEquals(testMachine, Helpers.getPrettyPrint(testMachine));
	}
}
