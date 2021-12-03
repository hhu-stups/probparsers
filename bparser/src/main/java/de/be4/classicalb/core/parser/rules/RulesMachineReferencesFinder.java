package de.be4.classicalb.core.parser.rules;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.be4.classicalb.core.parser.FileSearchPathProvider;
import de.be4.classicalb.core.parser.analysis.MachineClauseAdapter;
import de.be4.classicalb.core.parser.analysis.prolog.PackageName;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.node.AFileMachineReference;
import de.be4.classicalb.core.parser.node.AImportPackage;
import de.be4.classicalb.core.parser.node.AMachineHeader;
import de.be4.classicalb.core.parser.node.AMachineReference;
import de.be4.classicalb.core.parser.node.APackageParseUnit;
import de.be4.classicalb.core.parser.node.AReferencesMachineClause;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.PImportPackage;
import de.be4.classicalb.core.parser.node.PMachineReference;
import de.be4.classicalb.core.parser.node.TPragmaIdOrString;
import de.be4.classicalb.core.parser.util.Utils;

public class RulesMachineReferencesFinder extends MachineClauseAdapter {

	private final File mainFile;
	private final Node start;
	private final List<String> pathList = new ArrayList<>();
	private String machineName;
	private PackageName packageName;
	private File rootDirectory;
	private final List<RulesMachineReference> references;
	private final ArrayList<CheckException> errorList = new ArrayList<>();

	public RulesMachineReferencesFinder(File machineFile, Node node) {
		this.references = new ArrayList<>();
		this.mainFile = machineFile;
		this.start = node;
	}

	public void findReferencedMachines() throws BCompoundException {
		this.start.apply(this);
		if (!errorList.isEmpty()) {
			final List<BException> bExceptionList = new ArrayList<>();
			for (CheckException checkException : errorList) {
				final BException bException = new BException(mainFile.getAbsolutePath(), checkException);
				bExceptionList.add(bException);
			}
			throw new BCompoundException(bExceptionList);
		}
	}

	public String getName() {
		return machineName;
	}

	public File getProjectRootDirectory() {
		return this.rootDirectory;
	}

	public List<RulesMachineReference> getReferences() {
		return Collections.unmodifiableList(this.references);
	}

	@Override
	public void caseAMachineHeader(AMachineHeader node) {
		machineName = Utils.getTIdentifierListAsString(node.getName());
		if (mainFile != null) {
			final String fileNameWithoutExtension = Utils.getFileWithoutExtension(mainFile.getName());
			if (!machineName.equals(fileNameWithoutExtension)) {
				CheckException ch = new CheckException(
						String.format("RULES_MACHINE name must match the file name: '%s' vs '%s'", machineName,
								fileNameWithoutExtension),
						node);
				errorList.add(ch);
			}
		}
	}

	@Override
	public void caseAPackageParseUnit(APackageParseUnit node) {
		determineRootDirectory(node.getPackage(), node);
		List<PImportPackage> copy = new ArrayList<>(node.getImports());
		for (PImportPackage e : copy) {
			e.apply(this);
		}
		node.getParseUnit().apply(this);
		// delete this node
		node.replaceBy(node.getParseUnit());
	}

	@Override
	public void caseAImportPackage(AImportPackage node) {
		final File pathFile;
		try {
			pathFile = getPackageName(node.getPackage(), node).getFile(this.rootDirectory);
		} catch (CheckException e) {
			errorList.add(e);
			return;
		}
		final String path = pathFile.getAbsolutePath();
		if (!pathFile.exists()) {
			errorList.add(
					new CheckException(String.format("Imported package does not exist: %s", path), node.getPackage()));
			return;
		}
		if (this.pathList.contains(path)) {
			errorList.add(new CheckException(String.format("Duplicate package import: %s", node.getPackage().getText()),
					node));
			return;
		}

		this.pathList.add(path);
	}

	private void determineRootDirectory(final TPragmaIdOrString packageTerminal, final Node node) {
		try {
			this.packageName = getPackageName(packageTerminal, node);
		} catch (CheckException e) {
			errorList.add(e);
			return;
		}
		final File packageDir;
		try {
			packageDir = mainFile.getCanonicalFile().getParentFile();
		} catch (IOException e) {
			errorList.add(new CheckException(e.getMessage(), (Node) null, e));
			return;
		}
		try {
			rootDirectory = this.packageName.determineRootDirectory(packageDir);
		} catch (IllegalArgumentException e) {
			errorList.add(new CheckException(e.getMessage(), node, e));
		}
	}

	private static PackageName getPackageName(final TPragmaIdOrString packageTerminal, final Node node) throws CheckException {
		try {
			return PackageName.fromPossiblyQuotedName(packageTerminal.getText());
		} catch (IllegalArgumentException e) {
			throw new CheckException(e.getMessage(), node, e);
		}
	}

	// REFERENCES foo, bar
	@Override
	public void caseAReferencesMachineClause(AReferencesMachineClause node) {
		for (PMachineReference ref : node.getMachineReferences()) {
			registerMachineNames(ref);
		}
	}

	private void registerMachineNames(final PMachineReference machineReference) {
		if (machineReference instanceof AFileMachineReference) {
			registerMachineByFilePragma((AFileMachineReference) machineReference);

		} else {
			AMachineReference mchRef = (AMachineReference) machineReference;
			registerMachineReference(mchRef);

		}
	}

	private void registerMachineReference(AMachineReference mchRef) {
		String name = mchRef.getMachineName().get(0).getText();
		if (this.machineName.equals(name)) {
			errorList.add(new CheckException(String.format(
					"The reference '%s' has the same name as the machine in which it is contained.", name), mchRef));
		}
		try {
			final File file = lookupFile(mainFile.getParentFile(), name, mchRef);
			RulesMachineReference rulesMachineReference = new RulesMachineReference(file, name, mchRef);
			references.add(rulesMachineReference);
		} catch (CheckException e) {
			errorList.add(e);
		}
	}

	private void registerMachineByFilePragma(AFileMachineReference fileNode) {
		String filePath = fileNode.getFile().getText();
		if (Utils.isQuoted(filePath, '"')) {
			filePath = Utils.removeSurroundingQuotes(filePath, '"');
		}
		final AMachineReference ref = (AMachineReference) fileNode.getReference();
		final String name = ref.getMachineName().get(0).getText();
		File file = null;
		File tempFile = new File(filePath);
		if (tempFile.exists() && tempFile.isAbsolute() && !tempFile.isDirectory()) {
			file = tempFile;
		} else {
			tempFile = new File(mainFile.getParentFile(), filePath);
			if (tempFile.exists() && !tempFile.isDirectory()) {
				file = tempFile;
			}
		}
		if (tempFile.isDirectory()) {
			errorList.add(new CheckException(String.format("File '%s' is a directory.", filePath), fileNode.getFile()));
			return;
		} else if (file == null) {
			errorList.add(new CheckException(String.format("File '%s' does not exist.", filePath), fileNode.getFile()));
			return;
		} else {
			RulesMachineReference rulesMachineReference = new RulesMachineReference(file, name,
					fileNode.getReference());
			references.add(rulesMachineReference);
			return;
		}

	}

	private static final String[] SUFFICES = new String[] { ".rmch", ".mch" };

	private File lookupFile(final File parentMachineDirectory, final String name, final Node node)
			throws CheckException {
		for (final String suffix : SUFFICES) {
			try {
				return new FileSearchPathProvider(parentMachineDirectory.getPath(), name + suffix, this.pathList)
						.resolve();
			} catch (IOException e) {
				// could not resolve the combination of prefix, machineName and
				// suffix, trying next one
			}
		}
		throw new CheckException(String.format("Machine not found: '%s'", name), node);
	}
}
