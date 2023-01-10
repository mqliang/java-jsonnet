package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstLiteralBoolean represents true and false
public class AstLiteralBoolean extends AstBaseNode {
  boolean value;

  public AstLiteralBoolean(LocationRange loc, boolean value) {
    super(loc);
    this.value = value;
  }
}

