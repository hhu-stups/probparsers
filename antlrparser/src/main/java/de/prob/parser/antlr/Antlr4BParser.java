package de.prob.parser.antlr;

import files.*;
import files.BParser.StartContext;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.analysis.prolog.ASTProlog;
import de.be4.classicalb.core.parser.analysis.prolog.ClassicalPositionPrinter;
import de.be4.classicalb.core.parser.analysis.prolog.NodeIdAssignment;
import de.be4.classicalb.core.parser.exceptions.BParseException;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.util.PrettyPrinter;
import de.prob.parser.ast.nodes.MachineNode;
import de.prob.parser.ast.visitors.MachineContex;
import de.prob.parser.ast.visitors.TypeChecker;
import de.prob.parser.ast.visitors.TypeErrorException;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.PrologTermOutput;

public class Antlr4BParser {

	public static MachineNode createSemanticAST(String input) throws TypeErrorException {
		StartContext tree = parse(input);
		AstCreator astCreator = new AstCreator(tree);
		MachineNode machineNode = astCreator.getMachineNode();
		new MachineContex(machineNode);
		TypeChecker.typecheckMachineNode(machineNode);
		return machineNode;
	}

	public static void createProject(String input, String... machines) {
		StartContext tree = parse(input);
		AstCreator astCreator = new AstCreator(tree);
		MachineNode main = astCreator.getMachineNode();
		List<MachineNode> machineNodeList = new ArrayList<>();
		for (String string : machines) {
			StartContext tree2 = parse(string);
			AstCreator astCreator2 = new AstCreator(tree2);
			MachineNode mNode = astCreator2.getMachineNode();
			machineNodeList.add(mNode);
		}

		// TODO determine order

		List<MachineContex> scopeList = new ArrayList<>();
		for (MachineNode machineNode : machineNodeList) {
			MachineContex machineContex = new MachineContex(machineNode, scopeList);
			scopeList.add(machineContex);
		}
		MachineContex machineContex = new MachineContex(main, scopeList);
	}

	public static StartContext parse(String bString) {
		CodePointCharStream charStream = CharStreams.fromString(bString);

		BLexer lexer = new BLexer(charStream);
		// MyLexer myLexer = new MyLexer(fromString);

		// create a buffer of tokens pulled from the lexer
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		// BLexer.rulesGrammar = true;
		// create a parser that feeds off the tokens buffer

		BParser parser = new BParser(tokens);
		// RulesGrammar parser = new RulesGrammar(tokens);

		// parser.addErrorListener(new MyErrorListener());
		// parser.removeErrorListeners();
		parser.addErrorListener(new DiagnosticErrorListener());
		MyErrorListener myErrorListener = new MyErrorListener();
		parser.addErrorListener(myErrorListener);
		StartContext tree = null;

		tree = parser.start();

		// begin parsing at start rule
		// if (myErrorListener.exception != null) {
		// throw new RuntimeException(myErrorListener.exception);
		// }

		// System.out.println(tree.toStringTree(parser)); // print LISP-style
		// tree

		// PragmaListener pragmaListener = new PragmaListener(tokens);
		// ParseTreeWalker walker = new ParseTreeWalker();
		// walker.walk(pragmaListener, tree);

		// MyTreeListener listener = new MyTreeListener();
		// ParseTreeWalker walker2 = new ParseTreeWalker();
		// walker.walk(listener, tree);
		// System.out.println("-------------");

		return tree;
	}

}
