package de.hhu.stups.codegenerator;

import de.prob.parser.ast.nodes.DeclarationNode;
import de.prob.parser.ast.nodes.EnumeratedSetDeclarationNode;
import de.prob.parser.ast.nodes.EnumeratedSetElementNode;
import de.prob.parser.ast.nodes.MachineNode;
import de.prob.parser.ast.nodes.OperationNode;
import de.prob.parser.ast.nodes.expression.ExprNode;
import de.prob.parser.ast.nodes.expression.ExpressionOperatorNode;
import de.prob.parser.ast.nodes.expression.IdentifierExprNode;
import de.prob.parser.ast.nodes.expression.NumberNode;
import de.prob.parser.ast.nodes.expression.QuantifiedExpressionNode;
import de.prob.parser.ast.nodes.expression.SetComprehensionNode;
import de.prob.parser.ast.nodes.ltl.LTLBPredicateNode;
import de.prob.parser.ast.nodes.ltl.LTLInfixOperatorNode;
import de.prob.parser.ast.nodes.ltl.LTLKeywordNode;
import de.prob.parser.ast.nodes.ltl.LTLPrefixOperatorNode;
import de.prob.parser.ast.nodes.predicate.CastPredicateExpressionNode;
import de.prob.parser.ast.nodes.predicate.IdentifierPredicateNode;
import de.prob.parser.ast.nodes.predicate.PredicateOperatorNode;
import de.prob.parser.ast.nodes.predicate.PredicateOperatorWithExprArgsNode;
import de.prob.parser.ast.nodes.predicate.QuantifiedPredicateNode;
import de.prob.parser.ast.nodes.substitution.AnySubstitutionNode;
import de.prob.parser.ast.nodes.substitution.AssignSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.BecomesElementOfSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.BecomesSuchThatSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.ConditionSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.IfOrSelectSubstitutionsNode;
import de.prob.parser.ast.nodes.substitution.ListSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.OperationCallSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.SkipSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.SubstitutionNode;
import de.prob.parser.ast.nodes.substitution.WhileSubstitutionNode;
import de.prob.parser.ast.visitors.AbstractVisitor;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static de.hhu.stups.codegenerator.GeneratorMode.C;
import static de.hhu.stups.codegenerator.GeneratorMode.JAVA;

public class MachineGenerator implements AbstractVisitor<String, Void> {

	private static final STGroup JAVA_GROUP = new STGroupFile(MachineGenerator.class.getClassLoader()
			.getResource("de/hhu/stups/codegenerator/JavaTemplate.stg").getFile());

	private static final STGroup C_GROUP = new STGroupFile(
			MachineGenerator.class.getClassLoader().getResource("de/hhu/stups/codegenerator/CTemplate.stg").getFile());

	private static final Map<GeneratorMode, STGroup> TEMPLATE_MAP = new HashMap<>();

	static {
		TEMPLATE_MAP.put(JAVA, JAVA_GROUP);
		TEMPLATE_MAP.put(C, C_GROUP);
	}

	private List<DeclarationNode> locals;

	private Set<String> imports;

	private STGroup currentGroup;

	private Map<String, String> machineFromOperation;

	private String machineName;

	public MachineGenerator(GeneratorMode mode) {
		this.currentGroup = TEMPLATE_MAP.get(mode);
		this.locals = new ArrayList<>();
		this.imports = new HashSet<>();
		this.machineFromOperation = new HashMap<>();
	}

	public String generateMachine(MachineNode node) {
		this.machineName = node.getName();
		node.getMachineReferences()
				.forEach(reference -> reference.getMachineNode().getOperations()
						.forEach(operation -> machineFromOperation.put(operation.getName(), reference.getMachineName())));
		ST machine = currentGroup.getInstanceOf("machine");
		machine.add("imports", imports);
		machine.add("machine", NameHandler.handleMachineName(node.getName()));
		generateBody(node, machine);
		return machine.render();
	}

	private void generateBody(MachineNode node, ST machine) {
		machine.add("enums", generateEnumDeclarations(node));
		machine.add("sets", generateSetDeclarations(node));
		machine.add("declarations", visitDeclarations(node.getVariables()));
		machine.add("includes", generateIncludes(node));
		machine.add("initialization", visitInitialization(node.getInitialisation()));
		machine.add("operations", visitOperations(node.getOperations()));
	}

	private List<String> visitDeclarations(List<DeclarationNode> declarations) {
		return declarations.stream().map(this::generateGlobalDeclaration).collect(Collectors.toList());
	}

	private String generateGlobalDeclaration(DeclarationNode node) {
		ST declaration = currentGroup.getInstanceOf("global_declaration");
		declaration.add("type", TypeGenerator.generate(node.getType(), currentGroup, false));
		declaration.add("identifier", NameHandler.handle(node.getName(), currentGroup));
		return declaration.render();
	}

	private List<String> generateIncludes(MachineNode node) {
		return node.getMachineReferences().stream()
				.map(reference -> {
					ST declaration = currentGroup.getInstanceOf("include_declaration");
					String machine = reference.getMachineName();
					declaration.add("type", NameHandler.handleMachineName(machine));
					declaration.add("identifier", machine.toLowerCase());
					return declaration.render();
				})
				.collect(Collectors.toList());
	}

	private String visitInitialization(SubstitutionNode node) {
		ST initialization = currentGroup.getInstanceOf("initialization");
		initialization.add("body", visitSubstitutionNode(node, null));
		return initialization.render();
	}

	private List<String> visitOperations(List<OperationNode> operations) {
		return operations.stream().map(this::visitOperation).collect(Collectors.toList());
	}

	private String visitOperation(OperationNode node) {
		// TODO
		this.locals = node.getOutputParams();
		ST operation = OperationGenerator.generate(node, locals, currentGroup);
		operation.add("body", visitSubstitutionNode(node.getSubstitution(), null));
		return operation.render();
	}

	@Override
	public String visitExprNode(ExprNode node, Void expected) {
		TypeGenerator.addImport(node.getType(), imports, currentGroup);
		if (node instanceof NumberNode) {
			return visitNumberNode((NumberNode) node, expected);
		} else if (node instanceof ExpressionOperatorNode) {
			return visitExprOperatorNode((ExpressionOperatorNode) node, expected);
		} else if (node instanceof EnumeratedSetElementNode) {
			return visitEnumeratedSetElementNode((EnumeratedSetElementNode) node, expected);
		}
		return visitIdentifierExprNode((IdentifierExprNode) node, expected);
	}

	private List<String> generateEnumDeclarations(MachineNode node) {
		return node.getEnumaratedSets().stream().map(this::declareEnums).collect(Collectors.toList());
	}

	private List<String> generateSetDeclarations(MachineNode node) {
		return node.getEnumaratedSets().stream().map(this::visitEnumeratedSetDeclarationNode)
				.collect(Collectors.toList());
	}

	private String declareEnums(EnumeratedSetDeclarationNode node) {
		TypeGenerator.addImport(node.getElements().get(0).getType(), imports, currentGroup);
		ST enumDeclaration = currentGroup.getInstanceOf("set_enum_declaration");
		String name = NameHandler.handle(node.getSetDeclarationNode().getName(), currentGroup);
		enumDeclaration.add("name", name.substring(0, 1).toUpperCase() + name.substring(1));
		List<String> enums = node.getElements().stream().map(declaration -> declaration.getName().toUpperCase())
				.collect(Collectors.toList());
		enumDeclaration.add("enums", enums);
		return enumDeclaration.render();
	}

	public String visitEnumeratedSetDeclarationNode(EnumeratedSetDeclarationNode node) {
		// TODO
		TypeGenerator.addImport(node.getSetDeclarationNode().getType(), imports, currentGroup);
		ST setDeclaration = currentGroup.getInstanceOf("set_declaration");
		setDeclaration.add("identifier", NameHandler.handle(node.getSetDeclarationNode().getName(), currentGroup));
		List<String> enums = node.getElements().stream().map(declaration -> callEnum(node, declaration))
				.collect(Collectors.toList());
		setDeclaration.add("enums", enums);
		return setDeclaration.render();
	}

	public String callEnum(EnumeratedSetDeclarationNode setDeclarationNode, DeclarationNode enumNode) {
		ST enumST = currentGroup.getInstanceOf("enum_call");
		String name = setDeclarationNode.getSetDeclarationNode().getName();
		enumST.add("class", name.substring(0, 1).toUpperCase() + name.substring(1));
		enumST.add("identifier", enumNode.getName().toUpperCase());
		return enumST.render();
	}

	public String visitEnumeratedSetElementNode(EnumeratedSetElementNode node, Void expected) {
		String typeName = node.getType().toString();
		typeName = typeName.substring(0, 1).toUpperCase() + typeName.substring(1);
		ST element = currentGroup.getInstanceOf("set_element");
		element.add("set", typeName);
		element.add("element", node.getName().toUpperCase());
		return element.render();
	}

	@Override
	public String visitExprOperatorNode(ExpressionOperatorNode node, Void expected) {
		List<String> expressionList = node.getExpressionNodes().stream().map(expr -> visitExprNode(expr, expected))
				.collect(Collectors.toList());
		return OperatorGenerator.generateExpression(node, expressionList, currentGroup);
	}

	@Override
	public String visitIdentifierExprNode(IdentifierExprNode node, Void expected) {
		return IdentifierGeneratorHandler.generate(node, locals, currentGroup);
	}

	@Override
	public String visitCastPredicateExpressionNode(CastPredicateExpressionNode node, Void expected) {
		return null;
	}

	@Override
	public String visitNumberNode(NumberNode node, Void expected) {
		ST number = currentGroup.getInstanceOf("number");
		number.add("number", node.getValue().toString());
		return number.render();
	}

	@Override
	public String visitQuantifiedExpressionNode(QuantifiedExpressionNode node, Void expected) {
		return null;
	}

	@Override
	public String visitSetComprehensionNode(SetComprehensionNode node, Void expected) {
		return null;
	}

	@Override
	public String visitIdentifierPredicateNode(IdentifierPredicateNode node, Void expected) {
		return null;
	}

	@Override
	public String visitPredicateOperatorNode(PredicateOperatorNode node, Void expected) {
		List<String> expressionList = node.getPredicateArguments().stream()
				.map(expr -> visitPredicateNode(expr, expected)).collect(Collectors.toList());
		return OperatorGenerator.generatePredicate(node, expressionList, currentGroup);
	}

	@Override
	public String visitPredicateOperatorWithExprArgs(PredicateOperatorWithExprArgsNode node, Void expected) {
		List<String> expressionList = node.getExpressionNodes().stream().map(expr -> visitExprNode(expr, expected))
				.collect(Collectors.toList());
		return OperatorGenerator.generateBinary(node::getOperator, expressionList, currentGroup);
	}

	@Override
	public String visitQuantifiedPredicateNode(QuantifiedPredicateNode node, Void expected) {
		return null;
	}

	@Override
	public String visitIfOrSelectSubstitutionsNode(IfOrSelectSubstitutionsNode node, Void expected) {
		if (node.getOperator() == IfOrSelectSubstitutionsNode.Operator.SELECT) {
			return visitSelectSubstitution(node);
		}
		return visitIfSubstitution(node);
	}

	private String visitSelectSubstitution(IfOrSelectSubstitutionsNode node) {
		ST select = currentGroup.getInstanceOf("select");
		select.add("predicate", visitPredicateNode(node.getConditions().get(0), null));
		select.add("then", visitSubstitutionNode(node.getSubstitutions().get(0), null));
		return select.render();
	}

	private String visitIfSubstitution(IfOrSelectSubstitutionsNode node) {
		ST ifST = currentGroup.getInstanceOf("if");
		ifST.add("predicate", visitPredicateNode(node.getConditions().get(0), null));
		ifST.add("then", visitSubstitutionNode(node.getSubstitutions().get(0), null));
		ifST.add("else1", generateElseIfs(node));

		if (node.getElseSubstitution() != null) {
			ifST.add("else1", generateElse(node));
		}
		return ifST.render();
	}

	private List<String> generateElseIfs(IfOrSelectSubstitutionsNode node) {
		List<String> conditions = node.getConditions().subList(1, node.getConditions().size()).stream()
				.map(condition -> visitPredicateNode(condition, null)).collect(Collectors.toList());
		List<String> then = node.getSubstitutions().subList(1, node.getSubstitutions().size()).stream()
				.map(substitutionNode -> visitSubstitutionNode(substitutionNode, null)).collect(Collectors.toList());

		List<String> elseIfs = new ArrayList<>();

		for (int i = 0; i < conditions.size(); i++) {
			ST elseST = currentGroup.getInstanceOf("elseif");
			elseST.add("predicate", conditions.get(i));
			elseST.add("then", then.get(i));
			elseIfs.add(elseST.render());
		}

		return elseIfs;
	}

	private String generateElse(IfOrSelectSubstitutionsNode node) {
		ST elseST = currentGroup.getInstanceOf("else");
		elseST.add("then", visitSubstitutionNode(node.getElseSubstitution(), null));
		return elseST.render();
	}

	@Override
	public String visitSkipSubstitutionNode(SkipSubstitutionNode node, Void expected) {
		return null;
	}

	@Override
	public String visitConditionSubstitutionNode(ConditionSubstitutionNode node, Void expected) {
		return visitSubstitutionNode(node.getSubstitution(), expected);
	}

	@Override
	public String visitAnySubstitution(AnySubstitutionNode node, Void expected) {
		return null;
	}

	@Override
	public String visitAssignSubstitutionNode(AssignSubstitutionNode node, Void expected) {
		ST substitutions = currentGroup.getInstanceOf("assignments");
		List<String> assignments = new ArrayList<>();
		for (int i = 0; i < node.getLeftSide().size(); i++) {
			assignments.add(generateAssignment(node.getLeftSide().get(i), node.getRightSide().get(i)));
		}
		substitutions.add("assignments", assignments);
		return substitutions.render();
	}

	public String generateAssignment(ExprNode lhs, ExprNode rhs) {
		ST substitution = currentGroup.getInstanceOf("assignment");
		substitution.add("identifier", visitIdentifierExprNode((IdentifierExprNode) lhs, null));
		String typeCast = TypeGenerator.generate(rhs.getType(), currentGroup, true);
		substitution.add("val", typeCast + visitExprNode(rhs, null));
		return substitution.render();
	}

	@Override
	public String visitListSubstitutionNode(ListSubstitutionNode node, Void expected) {
		return visitSequentialSubstitutionNode(node);
	}

	public String visitSequentialSubstitutionNode(ListSubstitutionNode node) {
		List<String> substitutionCodes = node.getSubstitutions().stream()
				.map(substitutionNode -> visitSubstitutionNode(substitutionNode, null)).collect(Collectors.toList());
		return String.join("\n", substitutionCodes);
	}

	@Override
	public String visitBecomesElementOfSubstitutionNode(BecomesElementOfSubstitutionNode node, Void expected) {
		return null;
	}

	@Override
	public String visitBecomesSuchThatSubstitutionNode(BecomesSuchThatSubstitutionNode node, Void expected) {
		return null;
	}

	@Override
	public String visitLTLPrefixOperatorNode(LTLPrefixOperatorNode node, Void expected) {
		return null;
	}

	@Override
	public String visitLTLKeywordNode(LTLKeywordNode node, Void expected) {
		return null;
	}

	@Override
	public String visitLTLInfixOperatorNode(LTLInfixOperatorNode node, Void expected) {
		return null;
	}

	@Override
	public String visitLTLBPredicateNode(LTLBPredicateNode node, Void expected) {
		return null;
	}

	@Override
	public String visitSubstitutionIdentifierCallNode(OperationCallSubstitutionNode node, Void expected) {
		List<String> variables = node.getAssignedVariables().stream()
				.map(var -> visitExprNode(var, expected))
				.collect(Collectors.toList());
		String operationName = node.getOperationNode().getName();
		String machineName = machineFromOperation.get(operationName);
		ST functionCall;
		if(variables.size() > 0) {
			functionCall = currentGroup.getInstanceOf("operation_call_with_assignment");
			functionCall.add("var", variables.get(0));
		} else {
			functionCall = currentGroup.getInstanceOf("operation_call");
		}
		functionCall.add("machine", machineName.toLowerCase());
		functionCall.add("function", operationName.toLowerCase());
		functionCall.add("args", node.getArguments().stream().map(expr -> visitExprNode(expr, expected)).collect(Collectors.toList()));
		functionCall.add("this", machineName.equals(this.machineName));
		return functionCall.render();
	}

	@Override
	public String visitWhileSubstitutionNode(WhileSubstitutionNode node, Void expected) {
		ST whileST = currentGroup.getInstanceOf("while");
		whileST.add("predicate", visitPredicateNode(node.getCondition(), expected));
		whileST.add("then", visitSubstitutionNode(node.getBody(), expected));
		return whileST.render();
	}

}
