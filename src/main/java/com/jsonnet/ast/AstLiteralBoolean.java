package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstLiteralBoolean represents true and false
public class AstLiteralBoolean extends AstBaseNode {
  boolean value;

  AstLiteralBoolean(LocationRange loc) {
    super(loc);
  }
}

