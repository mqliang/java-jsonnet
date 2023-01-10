package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstLiteralNull represents the null keyword
public class AstLiteralNull extends AstBaseNode {
  public AstLiteralNull(LocationRange loc) {
    super(loc);
  }
}
