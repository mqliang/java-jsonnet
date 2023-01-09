package com.jsonnet.lexer;

public enum TokenKind {
  // A special token that indicates the token is invalid.
  tokenInvalid("invalid"),

  // Symbols
  tokenBraceL("{"),
  tokenBraceR("}"),
  tokenBracketL("["),
  tokenBracketR("]"),
  tokenComma(","),
  tokenDollar("$"),
  tokenDot("."),
  tokenParenL("("),
  tokenParenR(")"),
  tokenSemicolon(";"),

  // Arbitrary length lexemes
  tokenIdentifier("IDENTIFIER"),
  tokenNumber("NUMBER"),
  tokenOperator("OPERATOR"),
  tokenStringDouble("STRING_DOUBLE"),
  tokenStringSingle("STRING_SINGLE"),
  tokenStringBlock("STRING_BLOCK"),
  tokenVerbatimStringDouble("VERBATIM_STRING_DOUBLE"),
  tokenVerbatimStringSingle("VERBATIM_STRING_SINGLE"),

  // Keywords
  tokenAssert("assert"),
  tokenElse("else"),
  tokenError("error"),
  tokenFalse("false"),
  tokenFor("for"),
  tokenFunction("function"),
  tokenIf("if"),
  tokenImport("import"),
  tokenImportStr("importstr"),
  tokenImportBin("importbin"),
  tokenIn("in"),
  tokenLocal("local"),
  tokenNullLit("null"),
  tokenSelf("self"),
  tokenSuper("super"),
  tokenTailStrict("tailstrict"),
  tokenThen("then"),
  tokenTrue("true"),

  // A special token represents the EOF.
  tokenEndOfFile("end of file");

  private final String text;

  TokenKind(String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return text;
  }
}
