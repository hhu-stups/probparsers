package de.be4.classicalb.core.parser.grammars;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import de.be4.classicalb.core.parser.node.*;

public class RulesGrammar implements IGrammar {

	private static final String INSTANTIATION_ERROR_MESSAGE = "Cannot create an instance of class: ";

	public static final String RULES_MACHINE = "RULES_MACHINE";
	public static final String SUCCEEDED_RULE = "SUCCEEDED_RULE";
	public static final String SUCCEEDED_RULE_ERROR_TYPE = "SUCCEEDED_RULE_ERROR_TYPE";
	public static final String FAILED_RULE = "FAILED_RULE";
	public static final String FAILED_RULE_ERROR_TYPE = "FAILED_RULE_ERROR_TYPE";
	public static final String GET_RULE_COUNTEREXAMPLES = "GET_RULE_COUNTEREXAMPLES";
	public static final String FAILED_RULE_ALL_ERROR_TYPES = "FAILED_RULE_ALL_ERROR_TYPES";
	public static final String NOT_CHECKED_RULE = "NOT_CHECKED_RULE";
	public static final String DISABLED_RULE = "DISABLED_RULE";
	public static final String DEPENDS_ON_RULE = "DEPENDS_ON_RULE";
	public static final String DEPENDS_ON_COMPUTATION = "DEPENDS_ON_COMPUTATION";
	public static final String ERROR_TYPES = "ERROR_TYPES";
	public static final String RULEID = "RULEID";

	public static final String STRING_FORMAT = "STRING_FORMAT";
	public static final String ACTIVATION = "ACTIVATION";
	public static final String PRECONDITION = "PRECONDITION";
	public static final String POSTCONDITION = "POSTCONDITION";
	public static final String CLASSIFICATION = "CLASSIFICATION";
	public static final String REPLACES = "REPLACES";

	public static final String TAGS = "TAGS";

	private static RulesGrammar ruleExtension;

	public static RulesGrammar getInstance() {
		if (ruleExtension == null) {
			ruleExtension = new RulesGrammar();
		}
		return ruleExtension;
	}

	private RulesGrammar() {
		// singleton
	}

	private static final HashMap<String, Class<? extends Token>> map = new HashMap<>();
	static {
		add(TKwRule.class);
		add(TKwExpect.class);
		add(TKwOnSuccess.class);
		add(TKwUnchecked.class);
		add(TKwCounterexample.class);
		add(TKwRuleForAll.class);
		add(TKwFor.class);
		add(TKwComputation.class);
		add(TKwDefine.class);
		add(TKwType.class);
		add(TKwValue.class);
		add(TKwDummyValue.class);
		add(TKwFunction.class);
		add(TKwReferences.class);
		add(TKwRuleFail.class);
		add(TKwRuleErrorType.class);
		add(TKwBody.class);

		map.put(RULES_MACHINE, TMachine.class);
		map.put(SUCCEEDED_RULE, TKwPredicateOperator.class);
		map.put(SUCCEEDED_RULE_ERROR_TYPE, TKwPredicateOperator.class);
		map.put(FAILED_RULE, TKwPredicateOperator.class);
		map.put(FAILED_RULE_ERROR_TYPE, TKwPredicateOperator.class);
		map.put(FAILED_RULE_ALL_ERROR_TYPES, TKwPredicateOperator.class);
		map.put(NOT_CHECKED_RULE, TKwPredicateOperator.class);
		map.put(DISABLED_RULE, TKwPredicateOperator.class);

		map.put(DEPENDS_ON_RULE, TKwAttributeIdentifier.class);
		map.put(DEPENDS_ON_COMPUTATION, TKwAttributeIdentifier.class);
		map.put(RULEID, TKwAttributeIdentifier.class);
		map.put(ERROR_TYPES, TKwAttributeIdentifier.class);
		map.put(CLASSIFICATION, TKwAttributeIdentifier.class);
		map.put(TAGS, TKwAttributeIdentifier.class);
		map.put(REPLACES, TKwAttributeIdentifier.class);

		map.put(ACTIVATION, TKwPredicateAttribute.class);
		map.put(PRECONDITION, TKwPredicateAttribute.class);
		map.put(POSTCONDITION, TKwPredicateAttribute.class);

		map.put(GET_RULE_COUNTEREXAMPLES, TKwExpressionOperator.class);
		map.put(STRING_FORMAT, TKwExpressionOperator.class);
	}

	private static void add(Class<? extends Token> clazz) {
		try {
			map.put(clazz.getConstructor().newInstance().getText(), clazz);
		} catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
			throw new AssertionError(INSTANTIATION_ERROR_MESSAGE + clazz.getName(), e);
		}
	}

	@Override
	public boolean containsAlternativeDefinitionForToken(Token token) {
		return token instanceof TIdentifierLiteral && map.containsKey(token.getText());
	}

	@Override
	public Token createNewToken(Token token) {
		Class<? extends Token> clazz = map.get(token.getText());
		try {
			// default constructor
			Token newToken = clazz.getConstructor().newInstance();
			newToken.setLine(token.getLine());
			newToken.setPos(token.getPos());
			return newToken;
		} catch (NoSuchMethodException e) {
			// if the class has not default constructor we call the
			// construct with a text string as the single argument
			// e.g. TKwPredicateOperator(token.getText)
			Class<?>[] cArg = new Class<?>[] { String.class };
			try {
				Token newInstance = clazz.getDeclaredConstructor(cArg).newInstance(token.getText());
				newInstance.setLine(token.getLine());
				newInstance.setPos(token.getPos());
				return newInstance;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e1) {
				throw new AssertionError(INSTANTIATION_ERROR_MESSAGE + clazz.getName(), e1);
			}
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
			throw new AssertionError(INSTANTIATION_ERROR_MESSAGE + clazz.getName(), e);
		}
	}

}
