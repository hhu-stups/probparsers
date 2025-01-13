package de.be4.classicalb.core.parser.util;

import java.util.*;
import java.util.stream.Collectors;

import de.be4.classicalb.core.parser.IDefinitions;
import de.be4.classicalb.core.parser.node.*;
import de.hhu.stups.sablecc.patch.PositionedNode;
import de.hhu.stups.sablecc.patch.SourcePosition;

public final class ASTBuilder {

	public static final String FORCE = "FORCE";
	public static final String STRING_APPEND = "STRING_APPEND";
	public static final String CHOOSE = "CHOOSE";
	public static final String TO_STRING = "TO_STRING";
	public static final String FORMAT_TO_STRING = "FORMAT_TO_STRING";
	public static final String SORT = "SORT";
	public static final String PRINT = "PRINT";
	public static final String PREFERENCES_PREFIX = "SET_PREF_";

	private ASTBuilder() {
		// contains only static methods
	}

	public static PPredicate createConjunction(List<PPredicate> predList) {
		if (predList.isEmpty()) {
			throw new AssertionError();
		} else if (predList.size() == 1) {
			return predList.get(0);
		} else {
			PPredicate p = predList.get(0);
			for (int i = 1; i < predList.size(); i++) {
				p = new AConjunctPredicate(p, predList.get(i));
			}
			return p;
		}
	}

	public static PExpression createSetOfPExpression(PExpression pExpression, PositionedNode pos) {
		final ArrayList<PExpression> list = new ArrayList<>();
		list.add(pExpression.clone());
		return createPositionedNode(new ASetExtensionExpression(list), pos);
	}

	public static PExpression createSetOfPExpression(PExpression... pExpressions) {
		final ArrayList<PExpression> list = new ArrayList<>();
		for (PExpression pExpression : pExpressions) {
			list.add(pExpression.clone());
		}
		return new ASetExtensionExpression(list);
	}

	public static PExpression createNestedCouple(List<PExpression> pExpressions) {
		if (pExpressions.size() == 1) {
			return pExpressions.get(0);
		}
		final ArrayList<PExpression> list = new ArrayList<>();
		for (PExpression pExpression : pExpressions) {
			list.add(pExpression.clone());
		}
		return new ACoupleExpression(list);
	}

	public static PSubstitution createSequenceSubstitution(PSubstitution sub1, PSubstitution sub2,
			PSubstitution... subs) {
		List<PSubstitution> subList = new ArrayList<>();
		subList.add(sub1);
		subList.add(sub2);
		subList.addAll(Arrays.asList(subs));
		return new ASequenceSubstitution(subList);
	}

	public static <T extends PositionedNode> T createPositionedNode(T node, PositionedNode pos) {
		node.setStartPos(pos.getStartPos());
		node.setEndPos(pos.getEndPos());
		return node;
	}

	public static AStringExpression createStringExpression(String string) {
		return new AStringExpression(new TStringLiteral(string));
	}

	public static AIntegerExpression createIntegerExpression(int i) {
		return new AIntegerExpression(new TIntegerLiteral(Integer.toString(i)));
	}

	public static List<PSubstitution> createSubstitutionList(PSubstitution... pSubstitutions) {
		return new ArrayList<>(Arrays.asList(pSubstitutions));
	}

	public static List<PExpression> createExpressionList(PExpression... pExpressions) {
		return Arrays.stream(pExpressions)
				.map(oldNode -> createPositionedNode(oldNode.clone(), oldNode))
				.collect(Collectors.toList());
	}

	public static AIdentifierExpression createIdentifier(String name) {
		return new AIdentifierExpression(Collections.singletonList(new TIdentifierLiteral(name)));
	}

	public static AIdentifierExpression createIdentifier(String name, PositionedNode positionNode) {
		return createPositionedNode(createIdentifier(name), positionNode);
	}

	public static AIdentifierExpression createIdentifier(TIdentifierLiteral identifierLiteral) {
		return createIdentifier(identifierLiteral.getText(), identifierLiteral);
	}

	public static List<PExpression> createIdentifierList(String... names) {
		return Arrays.stream(names).map(ASTBuilder::createIdentifier).collect(Collectors.toList());
	}

	public static AEqualPredicate createEqualPredicate(TIdentifierLiteral old, final String value) {
		TIdentifierLiteral e = old.clone();
		return createPositionedNode(new AEqualPredicate(createIdentifier(e), createStringExpression(value)), e);
	}

	public static AAssignSubstitution createAssignNode(PExpression id, PExpression value) {
		return new AAssignSubstitution(Collections.singletonList(id), Collections.singletonList(value));
	}

	public static ADefinitionExpression callExternalFunction(String name, PExpression... parameters) {
		return new ADefinitionExpression(new TIdentifierLiteral(name), createExpressionList(parameters));
	}

	public static void addToStringDefinition(IDefinitions definitions) {
		if (definitions.containsDefinition(TO_STRING)) {
			return;
		}
		/*-
		 * TO_STRING(S) == "0"; 
		 * EXTERNAL_FUNCTION_TO_STRING(X) == X -->STRING;
		 */

		AExpressionDefinitionDefinition toStringDef = new AExpressionDefinitionDefinition();
		toStringDef.setName(new TIdentifierLiteral(TO_STRING));
		toStringDef.setParameters(createIdentifierList("S"));
		toStringDef.setRhs(new AStringExpression(new TStringLiteral("0")));
		definitions.addDefinition(toStringDef, IDefinitions.Type.Expression);

		AExpressionDefinitionDefinition toStringTypeDef = new AExpressionDefinitionDefinition();
		toStringTypeDef.setName(new TIdentifierLiteral("EXTERNAL_FUNCTION_TO_STRING"));
		toStringTypeDef.setParameters(createIdentifierList("X"));
		toStringTypeDef.setRhs(new ATotalFunctionExpression(createIdentifier("X"), new AStringSetExpression()));
		definitions.addDefinition(toStringTypeDef, IDefinitions.Type.Expression);
	}

	public static void addForceDefinition(IDefinitions iDefinitions) {
		if (iDefinitions.containsDefinition(FORCE)) {
			return;
		}
		/*-
		 * EXTERNAL_FUNCTION_FORCE(T) == T --> T; 
		 * FORCE(value) == value;
		 * forces evaluation of symbolic set representations 
		 * usage: FORCE({ x | x:1..100 & x mod 2 = 0 } )
		 */

		AExpressionDefinitionDefinition forceDef = new AExpressionDefinitionDefinition();
		forceDef.setName(new TIdentifierLiteral(FORCE));
		String value = "value";
		forceDef.setParameters(createIdentifierList(value));
		forceDef.setRhs(createIdentifier(value));
		iDefinitions.addDefinition(forceDef, IDefinitions.Type.Expression);

		AExpressionDefinitionDefinition forceDefType = new AExpressionDefinitionDefinition();
		forceDefType.setName(new TIdentifierLiteral("EXTERNAL_FUNCTION_" + FORCE));
		forceDefType.setParameters(createIdentifierList("T"));
		forceDefType.setRhs(new ATotalFunctionExpression(createIdentifier("T"), createIdentifier("T")));
		iDefinitions.addDefinition(forceDefType, IDefinitions.Type.Expression);
	}

	public static void addChooseDefinition(IDefinitions iDefinitions) {
		if (iDefinitions.containsDefinition(CHOOSE)) {
			return;
		}
		/*-
		 * TO_STRING(S) == "0";
		 * EXTERNAL_FUNCTION_TO_STRING(X) == (X --> STRING);
		 */

		AExpressionDefinitionDefinition chooseDef = new AExpressionDefinitionDefinition();
		chooseDef.setName(new TIdentifierLiteral(CHOOSE));
		chooseDef.setParameters(createIdentifierList("X"));
		chooseDef.setRhs(new AStringExpression(new TStringLiteral("a member of X")));
		iDefinitions.addDefinition(chooseDef, IDefinitions.Type.Expression);

		AExpressionDefinitionDefinition chooseDefType = new AExpressionDefinitionDefinition();
		chooseDefType.setName(new TIdentifierLiteral("EXTERNAL_FUNCTION_CHOOSE"));
		chooseDefType.setParameters(createIdentifierList("T"));
		chooseDefType.setRhs(
				new ATotalFunctionExpression(new APowSubsetExpression(createIdentifier("T")), createIdentifier("T")));
		iDefinitions.addDefinition(chooseDefType, IDefinitions.Type.Expression);
	}

	public static void addSortDefinition(IDefinitions iDefinitions) {
		if (iDefinitions.containsDefinition(SORT)) {
			return;
		}
		/*- SORT
		 *  SORT(X) == [];
		 *  EXTERNAL_FUNCTION_SORT(T) == POW(T)-->seq(T);
		 */
		AExpressionDefinitionDefinition sortDef = new AExpressionDefinitionDefinition();
		sortDef.setName(new TIdentifierLiteral(SORT));
		sortDef.setParameters(createIdentifierList("X"));
		sortDef.setRhs(new AEmptySequenceExpression());
		iDefinitions.addDefinition(sortDef, IDefinitions.Type.Expression);

		AExpressionDefinitionDefinition sortType = new AExpressionDefinitionDefinition();
		sortType.setName(new TIdentifierLiteral("EXTERNAL_FUNCTION_SORT"));
		sortType.setParameters(createIdentifierList("T"));
		sortType.setRhs(new ATotalFunctionExpression(new APowSubsetExpression(createIdentifier("T")),
				new ASeqExpression(createIdentifier("T"))));
		iDefinitions.addDefinition(sortType, IDefinitions.Type.Expression);

	}

	public static void addFormatToStringDefinition(IDefinitions iDefinitions) {
		if (iDefinitions.containsDefinition(FORMAT_TO_STRING)) {
			return;
		}
		/*-
		 * FORMAT_TO_STRING(MyFormatString,ListOfValues) == "0";
		 * EXTERNAL_FUNCTION_FORMAT_TO_STRING(TO_STRING_TYPE) == STRING*seq(TO_STRING_TYPE) --> STRING;
		 */

		AExpressionDefinitionDefinition formatDef = new AExpressionDefinitionDefinition();
		formatDef.setName(new TIdentifierLiteral(FORMAT_TO_STRING));
		formatDef.setParameters(createIdentifierList("S", "T"));
		formatDef.setRhs(new AStringExpression(new TStringLiteral("abc")));
		iDefinitions.addDefinition(formatDef, IDefinitions.Type.Expression);

		AExpressionDefinitionDefinition formatType = new AExpressionDefinitionDefinition();
		formatType.setName(new TIdentifierLiteral("EXTERNAL_FUNCTION_FORMAT_TO_STRING"));
		formatType.setParameters(createIdentifierList("T"));
		formatType.setRhs(new ATotalFunctionExpression(
				new AMultOrCartExpression(new AStringSetExpression(), new ASeqExpression(createIdentifier("T"))),
				new AStringSetExpression()));
		iDefinitions.addDefinition(formatType, IDefinitions.Type.Expression);
	}

	private static void addPreferenceDefinition(IDefinitions iDefinitions, String name, PExpression value) {
		AExpressionDefinitionDefinition def = new AExpressionDefinitionDefinition(new TIdentifierLiteral(PREFERENCES_PREFIX + name), Collections.emptyList(), value);
		iDefinitions.addDefinition(def, IDefinitions.Type.Expression);
	}

	public static void addBooleanPreferenceDefinition(IDefinitions iDefinitions, String name, boolean bool) {
		addPreferenceDefinition(iDefinitions, name, bool ? new ABooleanTrueExpression() : new ABooleanFalseExpression());
	}

	public static void addGeneralPreferenceDefinitions(IDefinitions iDefinitions, Map<String, String> map) {
		for (Map.Entry<String, String> entry : map.entrySet()) {
			addGeneralPreferenceDefinition(iDefinitions, entry.getKey(), entry.getValue());
		}
	}

	public static void addGeneralPreferenceDefinition(IDefinitions iDefinitions, String name, String value) {
		if (iDefinitions.containsDefinition(name)) {
			return;
		}

		PExpression expr;
		if ("TRUE".equals(value)) {
			expr = new ABooleanTrueExpression();
		} else if ("FALSE".equals(value)) {
			expr = new ABooleanFalseExpression();
		} else if (isInteger(value)) {
			expr = new AIntegerExpression(new TIntegerLiteral(value));
		} else {
			expr = new AStringExpression(new TStringLiteral(value));
		}
		addPreferenceDefinition(iDefinitions, name, expr);
	}

	private static boolean isInteger(String value) {
		try {
			Integer.parseInt(value);
			return true;
		} catch (NumberFormatException ignored) {
			return false;
		}
	}

}
