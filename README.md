# ProB Parsers Library

[![Build Status](https://gitlab.cs.uni-duesseldorf.de/stups/prob/probparsers/badges/develop/pipeline.svg)](https://gitlab.cs.uni-duesseldorf.de/stups/prob/probparsers/pipelines)

This is an umbrella project for the following parsers and libraries used by ProB:

* bparser: Parser for classical B.
* ltlparser: Parser for LTL formulas. The parser delegates formulas in `{ }` to a formalism specific parser, e.g. to the classical B parser. Also contains a parser for CTL.
* parserbase: Library for uniform access to the formal language parsers. This is used to allow embedding different languages into LTL formulas.
* unicode: Lexer that transforms Event-B expressions and predicates between ASCII, Unicode and LaTeX syntax.
* eventbstruct: Parser for the Camille structural syntax.
* theorymapping: Parser for theory mapping files (translation of Event-B operators to Prolog predicates).
* prologlib: Library to construct and manipulate well-formed Prolog terms.
* cliparser: Command-line interface for the B and LTL parsers. **For internal use only** by ProB's Prolog core (probcli).
* answerparser: Parser to read answers from the ProB Prolog core (probcli) in socket-server mode. **For internal use only** by the [ProB 2 Java API](https://github.com/hhu-stups/prob2_kernel).

The following subprojects were formerly part of this repo:

* eventbalg: Extended version of the Camille eventbstruct parser, with added support for procedures. Now moved into the [ProB 2 Event-B Algorithm DSL](https://github.com/hhu-stups/prob2-eventb-algorithm-dsl) library. The last release as part of probparsers was version 2.12.5.
* translator: Replaced by the [value-translator](https://github.com/hhu-stups/value-translator) project. The last release as part of probparsers was version 2.9.23.
* typechecker: Java-based classical B typechecker. No longer maintained - last release was version 2.9.32. Note that the normal ProB typechecker is part of prob_prolog and is unrelated to this subproject.
* voparser: Now maintained in the separate [vo_parser](https://gitlab.cs.uni-duesseldorf.de/general/stups/vo_parser) repository.

## Using

Releases are on [Maven Central](https://search.maven.org/search?q=g:de.hhu.stups), Snapshots on [Sonatype Snapshots](https://oss.sonatype.org/content/repositories/snapshots/).

You can include the different parsers in a Gradle build script like this:

```groovy
final parserVersion = "2.13.1"
dependencies {
	implementation group: "de.hhu.stups", name: "bparser", version: parserVersion
	implementation group: "de.hhu.stups", name: "ltlparser", version: parserVersion
	implementation group: "de.hhu.stups", name: "parserbase", version: parserVersion
}
```

Depending on your needs, you may remove parsers from the list or include additional ones. If you use more than one parser, you **must** use the same version number for all of them! This is ensured in the above code by storing the parser version in a variable.

You can also use the JAR probcliparser.jar built for ProB and distributed with ProB (inside its lib folder).
Here is how to obtain help for using the command-line parser:
```
$ java -jar probcliparser.jar --help
BParser (version 2.12.3, commit 99a64151f3bc82f987258619161831a8b9a7df01)
usage: BParser [options] <BMachine file>

Available options are:
-v          Verbose output during lexing and parsing
-time       Output time used for complete parsing process
-pp         Pretty Print in B format on standard output
-prolog     Show AST as Prolog term
-lineno     Put line numbers into prolog terms
-out        Specify output file
-version    Print the parser version and exit
-h          Print the parser help and exit
-help       Print the parser help and exit
--help      Print the parser help and exit
-compactpos Use new more compact Prolog position terms
-fastprolog Show AST as Prolog term for fast loading (Do not use this representation in your tool! It depends on internal representation of Sicstus Prolog and will very likely change arbitrarily in the future!)
-prepl      Enter parser-repl. Should only be used from inside ProB's Prolog Core.
-checkname  The name of a machine have to match file name (except for the file name extension)
```

You probably also want to set the path to the stdlib folder of ProB (containing files like LibraryStrings.def):
```
java -Dprob.stdlib=../stdlib/ -jar probcliparser.jar MyBMachine.mch
```

## Building

If you have made local modifications to the parser code and want to use the modified parser libraries in your project, you need to build the parser locally.

First, you need to ensure that your project depends on the latest version of the parser (which is defined at the top of the probparsers build.gradle). This version should end in `SNAPSHOT`. If you depend on an older version of the parser, your local changes will not take effect.

If your project uses Gradle, the best way to do this is using a *composite build*. This can be done by passing the option `--include-build /path/to/probparsers` to your project's Gradle build. For example, assuming that your Gradle project has a standard `run` task, you can run the following command in *your project's directory* (not in the probparsers directory!) to start it with the local version of your parser:

```sh
$ ./gradlew --include-build /path/to/probparsers run
```

If your project uses a different build tool (e. g. Maven), you should instead publish the parser libraries to your local Maven repository. This is done by running the following command in *the probparsers directory* (not in your project's directory):

```sh
$ ./gradlew publishToMavenLocal
```

### For probcli

If you are building probcli from source and want to include a local version of the parser, you can pass `SNAPSHOT=1 GRADLE="./gradlew --include-build /path/to/probparsers"` to the `make` call. This makes probcli's internal Gradle build use your local version of the parser instead of downloading a pre-built version.

## Testing

If you have made changes to any of the parsers, please run the test suites using `./gradlew check` to ensure that your changes are working as expected and do not break existing behavior. The tests are also run automatically by [our CI system](https://gitlab.cs.uni-duesseldorf.de/stups/prob/probparsers/pipelines).

## Bugs

Please report bugs and feature requests on our [bug tracker](https://github.com/hhu-stups/prob-issues/issues).

## Contributors

The libraries contain contributions from (in alphabetical order)
Jens Bendisposto, Marc Büngener, Fabian Fritz, Dominik Hansen, Sebastian Krings, Michael Leuschel, Daniel Plagge, David Schneider

## License

The ProB Parser Library source code is distributed under the Eclipse Public License - v 1.0 (see epl-v10.html)

The Parser Library comes with ABSOLUTELY NO WARRANTY OF ANY KIND !
This software is distributed in the hope that it will be useful
but WITHOUT ANY WARRANTY. The author(s) do not accept responsibility
to anyone for the consequences of using it or for whether it serves
any particular purpose or works at all. No warranty is made about
the software or its performance.

(c) 2011-2020 STUPS group, University of Düsseldorf
