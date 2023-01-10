package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstLiteralString represents a JSON string
public class AstLiteralString extends AstBaseNode {
  public String value;
  LiteralStringKind kind;
  String blockIndent;

  public AstLiteralString(LocationRange loc, String value, LiteralStringKind kind, String blockIndent) {
    super(loc);
    this.value =  value;
    this.kind = kind;
    this.blockIndent = blockIndent;
  }
}


