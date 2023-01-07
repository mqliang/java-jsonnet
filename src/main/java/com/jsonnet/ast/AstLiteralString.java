package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstLiteralString represents a JSON string
public class AstLiteralString extends AstBaseNode {
  String value;
  LiteralStringKind kind;
  String blockIndent;

  AstLiteralString(LocationRange loc) {
    super(loc);
  }

  enum astLiteralStringKind {
    astStringSingle, astStringDouble, astStringBlock
  }
}


