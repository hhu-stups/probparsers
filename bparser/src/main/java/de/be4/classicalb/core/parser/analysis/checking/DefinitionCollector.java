package de.be4.classicalb.core.parser.analysis.checking;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.be4.classicalb.core.parser.Definitions;
import de.be4.classicalb.core.parser.IDefinitions;
import de.be4.classicalb.core.parser.IDefinitions.Type;
import de.be4.classicalb.core.parser.PreParser;
import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.analysis.MachineClauseAdapter;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.node.ADefinitionsMachineClause;
import de.be4.classicalb.core.parser.node.AExpressionDefinitionDefinition;
import de.be4.classicalb.core.parser.node.APredicateDefinitionDefinition;
import de.be4.classicalb.core.parser.node.ASubstitutionDefinitionDefinition;
import de.be4.classicalb.core.parser.node.PDefinition;
import de.be4.classicalb.core.parser.node.Start;
import de.hhu.stups.sablecc.patch.SourcePosition;

/**
 * Collects the
 * {@link de.be4.classicalb.core.parser.node.APredicateDefinitionDefinition},
 * {@link de.be4.classicalb.core.parser.node.ASubstitutionDefinitionDefinition}
 * and
 * {@link de.be4.classicalb.core.parser.node.AExpressionDefinitionDefinition}
 * nodes, i.e. the declarations which were found by the main parser to store
 * them into an instance of {@link Definitions}.
 */
public class DefinitionCollector extends MachineClauseAdapter {

	private final IDefinitions definitions;
	private final List<CheckException> exceptions = new ArrayList<>();

	public DefinitionCollector(IDefinitions definitions) {
		this.definitions = definitions;
	}

	public void collectDefinitions(Start rootNode) {
		rootNode.apply(this);
	}

	public List<CheckException> getExceptions() {
		return this.exceptions;
	}

	@Override
	public void caseADefinitionsMachineClause(final ADefinitionsMachineClause definitions) {
		definitions.apply(new DepthFirstAdapter() {
			@Override
			public void caseAPredicateDefinitionDefinition(final APredicateDefinitionDefinition node) {
				final String defName = node.getName().getText();
				addDefinition(node, Type.Predicate, defName);
			}
			
			@Override
			public void caseASubstitutionDefinitionDefinition(final ASubstitutionDefinitionDefinition node) {
				final String defName = node.getName().getText();
				addDefinition(node, Type.Substitution, defName);
			}
			
			@Override
			public void caseAExpressionDefinitionDefinition(final AExpressionDefinitionDefinition node) {
				final String defName = node.getName().getText();
				Type type = PreParser.getExpressionDefinitionRhsType(node.getRhs());
				addDefinition(node, type, defName);
			}
		});
	}

	private void addDefinition(PDefinition def, Type type, String name) {
		if (definitions.containsDefinition(name)) {
			PDefinition existingDefinition = definitions.getDefinition(name);
			File file = definitions.getFile(name);
			StringBuilder sb = new StringBuilder();
			sb.append("Duplicate definition: ").append(name).append(".\n");
			SourcePosition firstPos = existingDefinition.getStartPos();
			sb.append("(First appearance at ").append(this.getPosition(firstPos));
			if (file != null) {
				sb.append(" in file: ").append(file.getAbsolutePath());
			}
			sb.append(")\n");
			CheckException e = new CheckException(sb.toString(), def);

			this.exceptions.add(e);

		} else {
			definitions.addDefinition(def, type, name);
		}
	}

	private String getPosition(SourcePosition pos) {
		return "Line: " + pos.getLine() + ", Column: " + pos.getPos();
	}

	public IDefinitions getDefinitions() {
		return definitions;
	}

}
