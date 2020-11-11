package de.prob.unicode;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import de.prob.unicode.lexer.Lexer;
import de.prob.unicode.lexer.LexerException;
import de.prob.unicode.node.EOF;
import de.prob.unicode.node.TAnyChar;
import de.prob.unicode.node.TBcmeq;
import de.prob.unicode.node.TBcmin;
import de.prob.unicode.node.TBcmsuch;
import de.prob.unicode.node.TBcomp;
import de.prob.unicode.node.TBfalse;
import de.prob.unicode.node.TBinter;
import de.prob.unicode.node.TBtrue;
import de.prob.unicode.node.TBunion;
import de.prob.unicode.node.TConv;
import de.prob.unicode.node.TCprod;
import de.prob.unicode.node.TDiv;
import de.prob.unicode.node.TDomres;
import de.prob.unicode.node.TDomsub;
import de.prob.unicode.node.TDotdot;
import de.prob.unicode.node.TDotdotdot;
import de.prob.unicode.node.TDprod;
import de.prob.unicode.node.TDrop;
import de.prob.unicode.node.TEmptyset;
import de.prob.unicode.node.TExists;
import de.prob.unicode.node.TExpn;
import de.prob.unicode.node.TFcomp;
import de.prob.unicode.node.TForall;
import de.prob.unicode.node.TGeq;
import de.prob.unicode.node.TIdentifierLiteral;
import de.prob.unicode.node.TIn;
import de.prob.unicode.node.TInter;
import de.prob.unicode.node.TIntg;
import de.prob.unicode.node.TLambda;
import de.prob.unicode.node.TLand;
import de.prob.unicode.node.TLbrace;
import de.prob.unicode.node.TLeq;
import de.prob.unicode.node.TLeqv;
import de.prob.unicode.node.TLimp;
import de.prob.unicode.node.TLnot;
import de.prob.unicode.node.TLor;
import de.prob.unicode.node.TMapsto;
import de.prob.unicode.node.TMid;
import de.prob.unicode.node.TMinus;
import de.prob.unicode.node.TMult;
import de.prob.unicode.node.TNat;
import de.prob.unicode.node.TNat1;
import de.prob.unicode.node.TNeq;
import de.prob.unicode.node.TNotin;
import de.prob.unicode.node.TNotsubset;
import de.prob.unicode.node.TNotsubseteq;
import de.prob.unicode.node.TOftype;
import de.prob.unicode.node.TOvl;
import de.prob.unicode.node.TPfun;
import de.prob.unicode.node.TPinj;
import de.prob.unicode.node.TPow;
import de.prob.unicode.node.TPow1;
import de.prob.unicode.node.TPprod;
import de.prob.unicode.node.TPsur;
import de.prob.unicode.node.TQdot;
import de.prob.unicode.node.TRanres;
import de.prob.unicode.node.TRansub;
import de.prob.unicode.node.TRbrace;
import de.prob.unicode.node.TRel;
import de.prob.unicode.node.TSeparator;
import de.prob.unicode.node.TSetminus;
import de.prob.unicode.node.TSrel;
import de.prob.unicode.node.TStrel;
import de.prob.unicode.node.TString;
import de.prob.unicode.node.TNumber;
import de.prob.unicode.node.TSubset;
import de.prob.unicode.node.TSubseteq;
import de.prob.unicode.node.TTake;
import de.prob.unicode.node.TTbij;
import de.prob.unicode.node.TTfun;
import de.prob.unicode.node.TTinj;
import de.prob.unicode.node.TTrel;
import de.prob.unicode.node.TTruncatedSetSize;
import de.prob.unicode.node.TTsur;
import de.prob.unicode.node.TTypeofClose;
import de.prob.unicode.node.TTypeofOpen;
import de.prob.unicode.node.TUnion;
import de.prob.unicode.node.TWhitespace;
import de.prob.unicode.node.Token;
import de.prob.unicode.node.TRealLiteral;
import de.prob.unicode.node.THexLiteral;

public class UnicodeTranslator {
	enum Encoding {
		ASCII, LATEX, UNICODE, RODIN_UNICODE
	}

	private static final class Translation {

		private final String ascii;
		private final String latex;
		private final String unicode;

		public Translation(final String ascii, final String latex, final String unicode) {
			this.ascii = ascii;
			this.latex = latex;
			this.unicode = unicode;
		}

		public String getAscii() {
			return ascii;
		}

		public String getLatex() {
			return latex;
		}

		public String getUnicode() {
			return unicode;
		}

	}

	private static final Map<Class<? extends Token>, Translation> m = new HashMap<>();

	static {
		m.put(TIn.class, new Translation(":", "\\in", "\u2208"));
		m.put(TNotsubseteq.class, new Translation("/<:", "\\notsubseteq", "\u2288"));
		m.put(TNotsubset.class, new Translation("/<<:", "\\notsubset", "\u2284"));
		m.put(TSubseteq.class, new Translation("<:", "\\subseteq", "\u2286"));
		m.put(TSetminus.class, new Translation("\\", "\\setminus", "\u2216"));
		m.put(TDotdot.class, new Translation("..", "\\upto", "\u2025"));
		m.put(TDotdotdot.class, new Translation("...", "\\ldots", "\u2026")); // ellipsis, used in shortened set values #Nr.{a,b,...,c,d}
		m.put(TNat1.class, new Translation("NAT1", "\\nat1", "\u21151"));
		m.put(TNat.class, new Translation("NAT", "\\nat", "\u2115"));
		m.put(TEmptyset.class, new Translation("{}", "\\emptyset", "\u2205"));
		m.put(TBcmsuch.class, new Translation(":|", "\\bcmsuch", ":\u2223"));
		m.put(TBfalse.class, new Translation("false", "\\bfalse", "\u22a5"));
		m.put(TForall.class, new Translation("!", "\\forall", "\u2200"));
		m.put(TExists.class, new Translation("#", "\\exists", "\u2203"));
		m.put(TMapsto.class, new Translation("|->", "\\mapsto", "\u21a6"));
		m.put(TBtrue.class, new Translation("true", "\\btrue", "\u22a4"));
		m.put(TSubset.class, new Translation("<<:", "\\subset", "\u2282"));
		m.put(TBunion.class, new Translation("\\/", "\\bunion", "\u222a"));
		m.put(TBinter.class, new Translation("/\\", "\\binter", "\u2229"));
		m.put(TDomres.class, new Translation("<|", "\\domres", "\u25c1"));
		m.put(TRanres.class, new Translation("|>", "\\ranres", "\u25b7"));
		m.put(TDomsub.class, new Translation("<<|", "\\domsub", "\u2a64"));
		m.put(TRansub.class, new Translation("|>>", "\\ransub", "\u2a65"));
		m.put(TLambda.class, new Translation("%", "\\lambda", "\u03bb"));
		m.put(TOftype.class, new Translation("oftype", "\\oftype", "\u2982"));
		m.put(TNotin.class, new Translation("/:", "\\notin", "\u2209"));
		m.put(TCprod.class, new Translation("**", "\\cprod", "\u00d7"));
		m.put(TUnion.class, new Translation("UNION", "\\Union", "\u22c3"));
		m.put(TInter.class, new Translation("INTER", "\\Inter", "\u22c2"));
		m.put(TFcomp.class, new Translation(";", "\\fcomp", "\u003b"));
		m.put(TBcomp.class, new Translation("circ", "\\bcomp", "\u2218"));
		m.put(TStrel.class, new Translation("<<->>", "\\strel", "\ue102"));
		m.put(TDprod.class, new Translation("><", "\\dprod", "\u2297"));
		m.put(TPprod.class, new Translation("||", "\\pprod", "\u2225"));
		m.put(TBcmeq.class, new Translation(":=", "\\bcmeq", "\u2254"));
		m.put(TBcmin.class, new Translation("::", "\\bcmin", ":\u2208"));
		m.put(TIntg.class, new Translation("INT", "\\intg", "\u2124"));
		m.put(TLand.class, new Translation("&", "\\land", "\u2227"));
		m.put(TLimp.class, new Translation("=>", "\\limp", "\u21d2"));
		m.put(TLeqv.class, new Translation("<=>", "\\leqv", "\u21d4"));
		m.put(TLnot.class, new Translation("not", "\\lnot", "\u00ac"));
		m.put(TQdot.class, new Translation(".", "\\qdot", "\u00b7"));
		m.put(TConv.class, new Translation("~", "\\conv", "\u223c"));
		m.put(TTrel.class, new Translation("<<->", "\\trel", "\ue100"));
		m.put(TSrel.class, new Translation("<->>", "\\srel", "\ue101"));
		m.put(TPfun.class, new Translation("+->", "\\pfun", "\u21f8"));
		m.put(TTfun.class, new Translation("-->", "\\tfun", "\u2192"));
		m.put(TPinj.class, new Translation(">+>", "\\pinj", "\u2914"));
		m.put(TTinj.class, new Translation(">->", "\\tinj", "\u21a3"));
		m.put(TPsur.class, new Translation("+>>", "\\psur", "\u2900"));
		m.put(TTsur.class, new Translation("->>", "\\tsur", "\u21a0"));
		m.put(TTbij.class, new Translation(">->>", "\\tbij", "\u2916"));
		m.put(TExpn.class, new Translation("^", "\\expn", "\u005e"));
		m.put(TLor.class, new Translation("or", "\\lor", "\u2228"));
		m.put(TPow1.class, new Translation("POW1", "\\pow1", "\u21191"));
		m.put(TPow.class, new Translation("POW", "\\pow", "\u2119"));
		m.put(TMid.class, new Translation("|", "\\mid", "\u2223")); // is the divides symbol, also generated by Rodin
		m.put(TNeq.class, new Translation("/=", "\\neq", "\u2260"));
		m.put(TRel.class, new Translation("<->", "\\rel", "\u2194"));
		m.put(TOvl.class, new Translation("<+", "\\ovl", "\ue103"));
		m.put(TLeq.class, new Translation("<=", "\\leq", "\u2264"));
		m.put(TGeq.class, new Translation(">=", "\\geq", "\u2265"));
		m.put(TDiv.class, new Translation("/", "\\div", "\u00f7"));
		m.put(TMult.class, new Translation("*", "*", "\u2217"));
		m.put(TMinus.class, new Translation("-", "-", "\u2212"));
		m.put(TLbrace.class, new Translation("{", "\\{", "{"));
		m.put(TRbrace.class, new Translation("}", "\\}", "}"));

		m.put(TTake.class, new Translation("/|\\", "/\\mid\\textbackslash", "/|\\"));
		m.put(TDrop.class, new Translation("\\|/", "\\textbackslash\\mid/", "\\|/"));
		m.put(TWhitespace.class, new Translation(" ", " ", " "));

		m.put(TTypeofOpen.class, new Translation("/*","/*", "/*"));
		m.put(TTypeofClose.class, new Translation("*/", "*/", "*/"));
	}

	public static void main(final String[] args) throws LexerException, IOException {
		String input = args[0];
		StringReader reader = new StringReader(input);
		PushbackReader r = new PushbackReader(reader, input.length());
		Lexer l = new Lexer(r);
		Token t;
		while (!((t = l.next()) instanceof EOF)) {
			String key = t.getClass().getSimpleName();
			System.out.print(key);
			System.out.print(" ");
		}
		System.out.println(UnicodeTranslator.toAscii(input));
		System.out.println(UnicodeTranslator.toLatex(input));
		System.out.println(UnicodeTranslator.toUnicode(input));
		System.out.println(UnicodeTranslator.toRodinUnicode(input));
	}

	public static String toAscii(final String s) {
		return translate(s, Encoding.ASCII);
	}

	public static String toLatex(final String s) {
		return translate(s, Encoding.LATEX);
	}

	public static String toUnicode(final String s) {
		return translate(s, Encoding.UNICODE);
	}
	
	/**
	 * Translate the given code to the Unicode-based syntax understood by the Rodin parser.
	 * This translation is mostly identical to {@link #toUnicode(String)},
	 * but performs some extra transformations,
	 * such as removing type comments (which the Rodin parser doesn't understand).
	 * 
	 * @param s the code to translate
	 * @return the code, translated to Rodin's Unicode syntax
	 */
	public static String toRodinUnicode(final String s) {
		return translate(s, Encoding.RODIN_UNICODE);
	}

	private static boolean isEventBIdentifierStart(final char c) {
		return Character.isUnicodeIdentifierStart(c) && c != 'λ';
	}
	
	private static boolean isEventBIdentifierPart(final char c) {
		return c == '\'' || (Character.isUnicodeIdentifierPart(c) && c != 'λ' && c != '·');
	}

	private static String translate(final String input, final Encoding target) {
		if (input.isEmpty()) {
			return "";
		}

		StringBuilder sb = new StringBuilder(input.length());
		StringReader reader = new StringReader(input);
		PushbackReader r = new PushbackReader(reader, input.length());
		Lexer l = new Lexer(r);

		Token t;
		boolean inTypeComment = false;
		try {
			while ((t = l.next()) != null && !(t instanceof EOF)) {
				// Drop type comments when translating to Unicode for Rodin
				// (the Rodin parser doesn't understand them).
				if (target == Encoding.RODIN_UNICODE) {
					if (inTypeComment) {
						if (t instanceof TTypeofClose) {
							inTypeComment = false;
						}
						continue;
					} else if (t instanceof TTypeofOpen) {
						inTypeComment = true;
						continue;
					}
				}

				final String translated;
				if (t instanceof TSeparator) {
					translated = t.getText();
				} else if (t instanceof TString) {
					if (target == Encoding.LATEX) {
						translated = "\\text{" + t.getText() + "}";
					} else {
						translated = t.getText();
					}
				} else if (t instanceof TTruncatedSetSize) {
					if (target == Encoding.LATEX) {
						translated = "\\" + t.getText();
					} else {
						translated = t.getText();
					}
				} else if (t instanceof TIdentifierLiteral) {
					if (target == Encoding.LATEX) {
						translated = "\\mathit{" + t.getText().replace("_", "\\_") + "}";
					} else {
						translated = t.getText();
					}
				} else if (t instanceof TAnyChar || t instanceof TNumber
						|| t instanceof TRealLiteral || t instanceof THexLiteral) {
					if (target == Encoding.LATEX) {
						translated = t.getText().replace("_", "\\_");
					} else {
						translated = t.getText();
					}
				} else {
					Translation translation = m.get(t.getClass());
					if(translation == null) { 
						// a Token which is not covered
						// translated = t.getText();
						throw new AssertionError("Unhandled Lexer token: " + t.getClass());
					} else if (target == Encoding.UNICODE || target == Encoding.RODIN_UNICODE) {
						translated = translation.getUnicode();
					} else if (target == Encoding.LATEX) {
						translated = translation.getLatex();
					} else if (target == Encoding.ASCII) {
						translated = translation.getAscii();
					} else {
						throw new AssertionError("Unhandled translation target: " + target);
					}
				}

				// Add a space if necessary to prevent two identifier-like tokens from being joined,
				// e. g. "a∨b" should translate to "a or b" and not "aorb",
				// but "a∧b" should still translate to "a&b" and not "a & b".
				if (
					sb.length() > 0 && isEventBIdentifierPart(sb.charAt(sb.length() - 1))
					&& !translated.isEmpty() && isEventBIdentifierPart(translated.charAt(0))
				) {
					sb.append(' ');
				}

				sb.append(translated);
			}
		} catch (LexerException | IOException e) {
			throw new AssertionError(e);
		}
		return sb.toString();
	}
}
