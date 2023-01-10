package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstDollar represents the $ keyword
public class AstDollar extends AstBaseNode {
  public AstDollar(LocationRange loc) {
    super(loc);
  }
}
