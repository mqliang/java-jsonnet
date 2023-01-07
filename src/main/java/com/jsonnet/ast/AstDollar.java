package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstDollar represents the $ keyword
public class AstDollar extends AstBaseNode {
  AstDollar(LocationRange loc) {
    super(loc);
  }
}
