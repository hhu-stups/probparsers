package de.be4.classicalb.core.parser.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.grammars.RulesGrammar;
import de.be4.classicalb.core.parser.node.ABooleanFalseExpression;
import de.be4.classicalb.core.parser.node.ABooleanTrueExpression;
import de.be4.classicalb.core.parser.node.AExpressionDefinitionDefinition;
import de.be4.classicalb.core.parser.node.AIdentifierExpression;
import de.be4.classicalb.core.parser.node.AIntegerExpression;
import de.be4.classicalb.core.parser.node.AOperatorExpression;
import de.be4.classicalb.core.parser.node.AOperatorPredicate;
import de.be4.classicalb.core.parser.node.APredicateDefinitionDefinition;
import de.be4.classicalb.core.parser.node.AStringExpression;
import de.be4.classicalb.core.parser.node.PExpression;
import de.be4.classicalb.core.parser.util.Utils;

/**
 * This class traverses the AST of the main machine of a RulesProject. Its
 * collects the configurations stored under the DEFINITIONS clause such as GOAL
 * and preferences.
 */
public class RulesMachineRunConfiguration {

	public static final String GOAL = "GOAL";
	final Map<String, AbstractOperation> allOperations;
	final RulesParseUnit mainModel;
	final Map<String, RuleGoalAssumption> rulesGoalAssumptions = new HashMap<>();
	private final Map<String, String> preferencesInMainMachine = new HashMap<>();

	public static RulesMachineRunConfiguration extractConfigurationOfMainModel(IModel mainModel,
			Map<String, AbstractOperation> allOperations) {
		RulesMachineRunConfiguration rulesMachineRunConfiguration = new RulesMachineRunConfiguration(mainModel,
				allOperations);
		rulesMachineRunConfiguration.collect();
		return rulesMachineRunConfiguration;
	}

	private RulesMachineRunConfiguration(IModel mainModel, Map<String, AbstractOperation> allOperations) {
		this.mainModel = (RulesParseUnit) mainModel;
		this.allOperations = allOperations;
	}

	public void collect() {
		DefinitionsFinder definitionsFinder = new DefinitionsFinder();
		mainModel.getStart().apply(definitionsFinder);
	}

	public Map<String, String> getPreferencesInModel() {
		return new HashMap<>(this.preferencesInMainMachine);
	}

	public Set<RuleGoalAssumption> getRulesGoalAssumptions() {
		return new HashSet<>(this.rulesGoalAssumptions.values());
	}

	class DefinitionsFinder extends DepthFirstAdapter {
		private static final String PROB_PREFERENCES_PREFIX = "SET_PREF_";

		@Override
		public void caseAExpressionDefinitionDefinition(AExpressionDefinitionDefinition node) {
			final String name = node.getName().getText();
			if (name.startsWith(PROB_PREFERENCES_PREFIX)) {
				String prefName = name.substring(PROB_PREFERENCES_PREFIX.length());
				if (node.getRhs() instanceof AIntegerExpression) {
					AIntegerExpression aIntExpr = (AIntegerExpression) node.getRhs();
					String value = aIntExpr.getLiteral().getText();
					preferencesInMainMachine.put(prefName, value);
				} else if (node.getRhs() instanceof AStringExpression) {
					AStringExpression aStringExpr = (AStringExpression) node.getRhs();
					String value = aStringExpr.getContent().getText();
					preferencesInMainMachine.put(prefName, value);
				} else if (node.getRhs() instanceof ABooleanTrueExpression) {
					preferencesInMainMachine.put(prefName, "TRUE");
				} else if (node.getRhs() instanceof ABooleanFalseExpression) {
					preferencesInMainMachine.put(prefName, "FALSE");
				}
			}
		}

		@Override
		public void caseAPredicateDefinitionDefinition(APredicateDefinitionDefinition node) {
			final String name = node.getName().getText();
			if (GOAL.equals(name)) {
				RulesInGoalFinder rulesInGoalFinder = new RulesInGoalFinder();
				node.getRhs().apply(rulesInGoalFinder);
			}
		}

	}

	class RulesInGoalFinder extends DepthFirstAdapter {
		@Override
		public void caseAOperatorExpression(AOperatorExpression node) {
			final String operatorName = node.getName().getText();
			if (RulesGrammar.GET_RULE_COUNTEREXAMPLES.equals(operatorName)) {
				RuleGoalAssumption ruleGoalAssumption = getRuleCoverage(node.getIdentifiers().get(0));
				ruleGoalAssumption.setCheckedForCounterexamples();
			}
		}

		private RuleGoalAssumption getRuleCoverage(PExpression pExpression) {
			AIdentifierExpression identifier = (AIdentifierExpression) pExpression;
			String ruleName = Utils.getTIdentifierListAsString(identifier.getIdentifier());
			return getRuleCoverage(ruleName);
		}

		private RuleGoalAssumption getRuleCoverage(String ruleName) {
			if (rulesGoalAssumptions.containsKey(ruleName)) {
				return rulesGoalAssumptions.get(ruleName);
			} else {
				RuleGoalAssumption ruleGoalAssumption = new RuleGoalAssumption(ruleName,
					(RuleOperation) allOperations.get(ruleName));
				rulesGoalAssumptions.put(ruleName, ruleGoalAssumption);
				return ruleGoalAssumption;
			}
		}

		@Override
		public void caseAOperatorPredicate(AOperatorPredicate node) {
			final List<PExpression> arguments = new ArrayList<>(node.getIdentifiers());
			final String operatorName = node.getName().getText();
			switch (operatorName) {
			case RulesGrammar.SUCCEEDED_RULE:
				getRuleCoverage(arguments.get(0)).setSuccessForAllErrorTypes();
				return;
			case RulesGrammar.FAILED_RULE:
				getRuleCoverage(arguments.get(0)).setFailedWithoutSpecificErrorType();
				return;
			case RulesGrammar.FAILED_RULE_ALL_ERROR_TYPES:
				getRuleCoverage(arguments.get(0)).setFailedForAllErrorTypes();
				return;
			case RulesGrammar.SUCCEEDED_RULE_ERROR_TYPE:
			case RulesGrammar.FAILED_RULE_ERROR_TYPE:
				RuleGoalAssumption ruleGoalAssumption = getRuleCoverage(arguments.get(0));
				AIntegerExpression intExpr = (AIntegerExpression) arguments.get(1);
				String text = intExpr.getLiteral().getText();
				int errorType = Integer.parseInt(text);
				if (RulesGrammar.SUCCEEDED_RULE_ERROR_TYPE.equals(operatorName)) {
					getRuleCoverage(arguments.get(0)).addErrorTypeAssumedToSucceed(errorType);
				} else {
					ruleGoalAssumption.addErrorTypeAssumedToFail(errorType);
				}
				return;
			default:
				// do nothing, e.g. for DISABLE_RULE
			}

		}

	}

	public static class RuleGoalAssumption {
		final String name;
		final RuleOperation ruleOperation;
		final HashSet<Integer> errorTypesAssumedToFail = new HashSet<>();
		final HashSet<Integer> errorTypesAssumedToSucceed = new HashSet<>();
		boolean checkedForCounterexamples = false;

		public RuleGoalAssumption(String name, RuleOperation ruleOperation) {
			this.name = name;
			this.ruleOperation = ruleOperation;
		}

		public void setCheckedForCounterexamples() {
			this.checkedForCounterexamples = true;
		}

		public void setFailedWithoutSpecificErrorType() {
			Integer n = ruleOperation.getNumberOfErrorTypes();
			if (n == 1) {
				errorTypesAssumedToFail.add(1);
			}
			// otherwise we have no information about the error type
		}

		public void setFailedForAllErrorTypes() {
			Integer n = ruleOperation.getNumberOfErrorTypes();
			for (int i = 1; i <= n; i++) {
				errorTypesAssumedToFail.add(i);
			}
		}

		public void setSuccessForAllErrorTypes() {
			Integer n = ruleOperation.getNumberOfErrorTypes();
			for (int i = 1; i <= n; i++) {
				errorTypesAssumedToSucceed.add(i);
			}
		}

		public String getRuleName() {
			return this.name;
		}

		public RuleOperation getRuleOperation() {
			return this.ruleOperation;
		}

		public void addErrorTypeAssumedToFail(Integer i) {
			this.errorTypesAssumedToFail.add(i);
		}

		public void addErrorTypeAssumedToSucceed(Integer i) {
			this.errorTypesAssumedToSucceed.add(i);
		}

		public Set<Integer> getErrorTypesAssumedToFail() {
			return new HashSet<>(errorTypesAssumedToFail);
		}

		public Set<Integer> getErrorTypesAssumedToSucceed() {
			return new HashSet<>(errorTypesAssumedToSucceed);
		}

		public boolean isCheckedForCounterexamples() {
			return this.checkedForCounterexamples;

		}

	}

}
