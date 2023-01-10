package com.jsonnet;

import com.google.common.collect.ImmutableList;
import com.jsonnet.common.StaticErrorException;
import com.jsonnet.lexer.Lexer;
import com.jsonnet.lexer.Token;
import com.jsonnet.lexer.TokenKind;
import java.util.LinkedList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.jsonnet.lexer.TokenKind.*;


public class TestLexer {

  static class testCase {
    String name;
    String input;
    List<Token> tokens;
    String errString;
    public testCase(String name, String input, List<Token> tokens, String errString) {
      this.name = name;
      this.input = input;
      this.tokens = tokens;
      this.errString = errString;
    }
  }

  public static Token newToken(TokenKind kind, String data) {
    return new Token(kind, null, data, null, null, null);
  }

  static Token tEOF = newToken(tokenEndOfFile, "");

  static boolean tokensEqual(List<Token> ts1, List<Token> ts2) {
    if (ts1.size() != ts2.size()) {
      return false;
    }
    for (int i = 0; i < ts1.size(); i++) {
      Token t1 = ts1.get(i);
      Token t2 = ts2.get(i);
      if (!t1.toString().equals(t2.toString())) {
        return false;
      }
    }
    return true;
  }

  @Test
  public void testLexSucceededCases() {
    List<testCase> testCases = ImmutableList.of(
        new testCase("empty", "", ImmutableList.of(), ""),
        new testCase("whitespace", "  \t\n\r\r\n", ImmutableList.of(), ""),
        new testCase("brace L", "{", ImmutableList.of(newToken(tokenBraceL, "{")), ""),
        new testCase("brace R", "}", ImmutableList.of(newToken(tokenBraceR, "}")), ""),
        new testCase("bracket L", "[", ImmutableList.of(newToken(tokenBracketL, "[")), ""),
        new testCase("bracket R", "]", ImmutableList.of(newToken(tokenBracketR, "]")), ""),
        new testCase("colon", ":", ImmutableList.of(newToken(tokenOperator, ":")), ""),
        new testCase("colon2", "::", ImmutableList.of(newToken(tokenOperator, "::")), ""),
        new testCase("colon3", ":::", ImmutableList.of(newToken(tokenOperator, ":::")), ""),
        new testCase("arrow right", "->", ImmutableList.of(newToken(tokenOperator, "->")), ""),
        new testCase("less than minus", "<-", ImmutableList.of(newToken(tokenOperator, "<"), newToken(tokenOperator, "-")), ""),
        new testCase("comma", ",", ImmutableList.of(newToken(tokenComma, ",")), ""),
        new testCase("dollar", "$", ImmutableList.of(newToken(tokenDollar, "$")), ""),
        new testCase("dot", ".", ImmutableList.of(newToken(tokenDot, ".")), ""),
        new testCase("paren L", "(", ImmutableList.of(newToken(tokenParenL, "(")), ""),
        new testCase("paren R", ")", ImmutableList.of(newToken(tokenParenR, ")")), ""),
        new testCase("semicolon", ";", ImmutableList.of(newToken(tokenSemicolon, ";")), ""),
        new testCase("not 1", "!", ImmutableList.of(newToken(tokenOperator, "!")), ""),
        new testCase("not 2", "! ", ImmutableList.of(newToken(tokenOperator, "!")), ""),
        new testCase("not equal", "!=", ImmutableList.of(newToken(tokenOperator, "!=")), ""),
        new testCase("tilde", "~", ImmutableList.of(newToken(tokenOperator, "~")), ""),
        new testCase("plus", "+", ImmutableList.of(newToken(tokenOperator, "+")), ""),
        new testCase("minus", "-", ImmutableList.of(newToken(tokenOperator, "-")), ""),
        new testCase("number 0", "0", ImmutableList.of(newToken(tokenNumber, "0")), ""),
        new testCase("number 1", "1", ImmutableList.of(newToken(tokenNumber, "1")), ""),
        new testCase("number 1.0", "1.0", ImmutableList.of(newToken(tokenNumber, "1.0")), ""),
        new testCase("number 0.1", "0.1", ImmutableList.of(newToken(tokenNumber, "0.1")), ""),
        new testCase("number 0e100", "0e100", ImmutableList.of(newToken(tokenNumber, "0e100")), ""),
        new testCase("number 1e100", "1e100", ImmutableList.of(newToken(tokenNumber, "1e100")), ""),
        new testCase("number 1.1e100", "1.1e100", ImmutableList.of(newToken(tokenNumber, "1.1e100")), ""),
        new testCase("number 1.1e-100", "1.1e-100", ImmutableList.of(newToken(tokenNumber, "1.1e-100")), ""),
        new testCase("number 1.1e+100", "1.1e+100", ImmutableList.of(newToken(tokenNumber, "1.1e+100")), ""),
        new testCase("number 0100", "0100", ImmutableList.of(newToken(tokenNumber, "0"), newToken(tokenNumber, "100")), ""),
        new testCase("number 10+10", "10+10", ImmutableList.of(newToken(tokenNumber, "10"), newToken(tokenOperator, "+"), newToken(tokenNumber, "10")), ""),

        new testCase("double string \"hi\"", "\"hi\"", ImmutableList.of(newToken(tokenStringDouble, "hi")), ""),
        new testCase("double string \"hi nl\"", "\"hi\n\"", ImmutableList.of(newToken(tokenStringDouble, "hi\n")), ""),
        new testCase("double string \"hi\\\"\"", "\"hi\\\"\"", ImmutableList.of(newToken(tokenStringDouble, "hi\\\"")), ""),
        new testCase("double string \"hi\\nl\"", "\"hi\\\n\"", ImmutableList.of(newToken(tokenStringDouble, "hi\\\n")), ""),

        new testCase("single string 'hi'", "'hi'", ImmutableList.of(newToken(tokenStringSingle, "hi")), ""),
        new testCase("single string 'hi nl'", "'hi\n'", ImmutableList.of(newToken(tokenStringSingle, "hi\n")), ""),
        new testCase("single string 'hi\\''", "'hi\\''", ImmutableList.of(newToken(tokenStringSingle, "hi\\'")), ""),
        new testCase("single string 'hi\\nl'", "'hi\\\n'", ImmutableList.of(newToken(tokenStringSingle, "hi\\\n")), ""),

        new testCase("assert", "assert", ImmutableList.of(newToken(tokenAssert, "assert")), ""),
        new testCase("else", "else", ImmutableList.of(newToken(tokenElse, "else")), ""),
        new testCase("error", "error", ImmutableList.of(newToken(tokenError, "error")), ""),
        new testCase("false", "false", ImmutableList.of(newToken(tokenFalse, "false")), ""),
        new testCase("for", "for", ImmutableList.of(newToken(tokenFor, "for")), ""),
        new testCase("function", "function", ImmutableList.of(newToken(tokenFunction, "function")), ""),
        new testCase("if", "if", ImmutableList.of(newToken(tokenIf, "if")), ""),
        new testCase("import", "import", ImmutableList.of(newToken(tokenImport, "import")), ""),
        new testCase("importstr", "importstr", ImmutableList.of(newToken(tokenImportStr, "importstr")), ""),
        new testCase("in", "in", ImmutableList.of(newToken(tokenIn, "in")), ""),
        new testCase("local", "local", ImmutableList.of(newToken(tokenLocal, "local")), ""),
        new testCase("null", "null", ImmutableList.of(newToken(tokenNullLit, "null")), ""),
        new testCase("self", "self", ImmutableList.of(newToken(tokenSelf, "self")), ""),
        new testCase("super", "super", ImmutableList.of(newToken(tokenSuper, "super")), ""),
        new testCase("tailstrict", "tailstrict", ImmutableList.of(newToken(tokenTailStrict, "tailstrict")), ""),
        new testCase("then", "then", ImmutableList.of(newToken(tokenThen, "then")), ""),
        new testCase("true", "true", ImmutableList.of(newToken(tokenTrue, "true")), ""),

        new testCase("identifier", "foobar", ImmutableList.of(newToken(tokenIdentifier, "foobar")), ""),

        new testCase("c++ comment", "// hi", ImmutableList.of(), ""),  // This test doesn't look at fodder (yet?)
        new testCase("hash comment", "# hi", ImmutableList.of(), ""),  // This test doesn't look at fodder (yet?)
        new testCase("c comment", "/* hi */", ImmutableList.of(), ""), // This test doesn't look at fodder (yet?)

        new testCase("block string spaces", "|||\n" + "  test\n" + "    more\n" + "  |||\n" + "    foo\n" + "|||",
            ImmutableList.of(newToken(tokenStringBlock, "test\n  more\n|||\n  foo\n")), ""),
        new testCase("block string tabs", "|||\n" + "\ttest\n" + "\t  more\n" + "\t|||\n" + "\t  foo\n" + "|||",
            ImmutableList.of(newToken(tokenStringBlock, "test\n  more\n|||\n  foo\n")), ""),
        new testCase("block string mixed",
            "|||\n" + "\t  \ttest\n" + "\t  \t  more\n" + "\t  \t|||\n" + "\t  \t  foo\n" + "|||",
            ImmutableList.of(newToken(tokenStringBlock, "test\n  more\n|||\n  foo\n")), ""),
        new testCase("block string blanks", "|||\n\n" + "\ttest\n\n\n" + "\t  more\n" + "\t|||\n" + "\t  foo\n" + "|||",
            ImmutableList.of(newToken(tokenStringBlock, "\ntest\n\n\n  more\n|||\n  foo\n")), ""),

        new testCase("op *", "*", ImmutableList.of(newToken(tokenOperator, "*")), ""),
        new testCase("op /", "/", ImmutableList.of(newToken(tokenOperator, "/")), ""),
        new testCase("op %", "%", ImmutableList.of(newToken(tokenOperator, "%")), ""),
        new testCase("op &", "&", ImmutableList.of(newToken(tokenOperator, "&")), ""),
        new testCase("op |", "|", ImmutableList.of(newToken(tokenOperator, "|")), ""),
        new testCase("op ^", "^", ImmutableList.of(newToken(tokenOperator, "^")), ""),
        new testCase("op =", "=", ImmutableList.of(newToken(tokenOperator, "=")), ""),
        new testCase("op <", "<", ImmutableList.of(newToken(tokenOperator, "<")), ""),
        new testCase("op >", ">", ImmutableList.of(newToken(tokenOperator, ">")), ""),
        new testCase("op >==|", ">==|", ImmutableList.of(newToken(tokenOperator, ">==|")), ""));

    for (testCase t : testCases) {
      // Copy the test tokens and append an EOF token
      List<Token> testTokens = new LinkedList<>(t.tokens);
      testTokens.add(tEOF);

      List<Token> tokens = new Lexer(t.name, t.input).lex();
      if (!tokensEqual(tokens, testTokens)) {
        Assert.fail(String.format("Case %s failed, expected: %s, got: %s", t.name, testTokens, tokens));
      }
    }
  }

  @Test
  public void testLexFailedCases() {
    List<testCase> testCases = ImmutableList.of(new testCase("number 1.+3", "1.+3", ImmutableList.of(),
            "number 1.+3:1:3 Couldn't lex number, junk after decimal point: '+'"),
        new testCase("number 1e!", "1e!", ImmutableList.of(),
            "number 1e!:1:3 Couldn't lex number, junk after 'E': '!'"),
        new testCase("number 1e+!", "1e+!", ImmutableList.of(),
            "number 1e+!:1:4 Couldn't lex number, junk after exponent sign: '!'"),

        new testCase("double string \"hi", "\"hi", ImmutableList.of(), "double string \"hi:1:1 Unterminated String"),
        new testCase("single string 'hi", "'hi", ImmutableList.of(), "single string 'hi:1:1 Unterminated String"),

        new testCase("c comment no term", "/* hi", ImmutableList.of(),
            "c comment no term:1:1 Multi-line comment has no terminating */"),
        // This test doesn't look at fodder (yet?)
        new testCase("block string bad indent", "|||\n" + "  test\n" + " foo\n" + "|||", ImmutableList.of(),
            "block string bad indent:1:1 Text block not terminated with |||"),
        new testCase("block string eof", "|||\n" + "  test", ImmutableList.of(), "block string eof:1:1 Unexpected EOF"),
        new testCase("block string not term", "|||\n" + "  test\n", ImmutableList.of(),
            "block string not term:1:1 Text block not terminated with |||"),
        new testCase("block string no ws", "|||\n" + "test\n" + "|||", ImmutableList.of(),
            "block string no ws:1:1 Text block's first line must start with whitespace"),

        new testCase("junk", "\uD83D\uDCA9", ImmutableList.of(), "junk:1:1 Could not lex the character \uD83D\uDCA9"));

    for (testCase t : testCases) {
      try {
        new Lexer(t.name, t.input).lex();
        Assert.fail("Case %s failed, exception expected, but succeeded");
      } catch (StaticErrorException e) {
        if (!e.toString().equals(t.errString)) {
          Assert.fail(String.format("Case %s failed, expected error message: %s, got: %s", t.name, t.errString, e));
        }
      }
    }
  }
}
