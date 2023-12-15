# this is just a simple reminder on what kind of gradle tasks that can be run
parseable:
	gradle test --tests de.be4.classicalb.core.parser.ParsableMachineTest --info
test2:
	gradle test --tests de.be4.classicalb.core.parser.ParseableButProblematicOnWindowsUnixLF --info
rules:
	gradle test --tests de.be4.classicalb.core.parser.rules.RulesMachineFilesTest --info
tokens:
	gradle test --tests de.be4.classicalb.core.parser.SyntaxErrorsDetectedOnTokenStreamTest --info
defsf:
	gradle test --tests de.be4.classicalb.core.parser.definitions.DefinitionFilesTest --info
defs:
	gradle test --tests de.be4.classicalb.core.parser.definitions.DefinitionsTest --info
deferrss:
	gradle test --tests de.be4.classicalb.core.parser.definitions.DefinitionsErrorsTest --info
expr:
	gradle test --tests de.be4.classicalb.core.parser.ExpressionTest --info
comments:
	gradle test --tests de.be4.classicalb.core.parser.CommentsTest --info
test3:
	gradle test --tests de.be4.classicalb.core.parser.SyntaxErrorsDetectedOnTokenStreamTest --info
ext:
	gradle test --tests de.be4.classicalb.core.parser.languageextension.SyntaxExtensionTest--info
extref:
	gradle test --tests de.be4.classicalb.core.parser.languageextension.RefinedOperationTest --info
msgs:
	gradle test --tests de.be4.classicalb.core.parser.ErrorMessagesTest  --info
slnc:
	gradle test --tests de.be4.classicalb.core.parser.StringLiteralNotClosedTest  --info
ast:
	gradle test --tests de.be4.classicalb.core.parser.ASTPrologTest  --info
	gradle test --tests de.be4.classicalb.core.parser.CreateFreetypeTest  --info

count:
	@echo "Line count for Java files:"
	cd src; find . -name '*.java' | xargs wc -l
	@echo "Line count for SableCC files:"
	cd src; find . -name '*.scc' | xargs wc -l
	@echo "Number of Java files:"
	cd src; find . -name '*.java' | wc -l
	@echo "Number of SableCC files:"
	cd src; find . -name '*.scc' | wc -l