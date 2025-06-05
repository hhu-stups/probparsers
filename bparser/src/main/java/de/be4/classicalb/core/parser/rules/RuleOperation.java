package de.be4.classicalb.core.parser.rules;

import java.util.List;

import de.be4.classicalb.core.parser.analysis.prolog.MachineReference;
import de.be4.classicalb.core.parser.node.AIdentifierExpression;
import de.be4.classicalb.core.parser.node.AIntegerExpression;
import de.be4.classicalb.core.parser.node.TIdentifierLiteral;

public class RuleOperation extends AbstractOperation {
	private AIdentifierExpression ruleId;
	private AIntegerExpression errorTypes;
	private String counterExampleVariableName;
	private String classification;
	private String successfulVariableName;
	private String uncheckedVariableName;

	public RuleOperation(TIdentifierLiteral ruleName, String fileName, String machineName,
			List<MachineReference> machineReferences) {
		super(ruleName, fileName, machineName, machineReferences);
	}

	public Integer getNumberOfErrorTypes() {
		if (this.errorTypes == null) {
			return 1;
		} else {
			final String text = errorTypes.getLiteral().getText();
			return Integer.parseInt(text);
		}
	}

	public void setRuleId(AIdentifierExpression ruleId) {
		this.ruleId = ruleId;
	}

	public void setErrorTypes(AIntegerExpression aIntegerExpression) {
		this.errorTypes = aIntegerExpression;
	}

	public String getRuleIdString() {
		if (ruleId == null) {
			return null;
		} else {
			return ruleId.getIdentifier().getFirst().getText();
		}
	}

	public void setCounterExampleVariableName(String name) {
		this.counterExampleVariableName = name;
	}

	public String getCounterExampleVariableName() {
		return this.counterExampleVariableName;
	}

	public void setClassification(String string) {
		this.classification = string;
	}

	public String getClassification() {
		return this.classification;
	}

	public void setSuccessfulVariableName(String name) {
		this.successfulVariableName = name;
	}

	public String getSuccessfulVariableName() {
		return this.successfulVariableName;
	}

	public void setUncheckedVariableName(String name) {
		this.uncheckedVariableName = name;
	}

	public String getUncheckedVariableName() {
		return this.uncheckedVariableName;
	}

}
