package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstError represents the error e.
public class AstError extends AstBaseNode {
  AstNode expr;

  public AstError(LocationRange loc, AstNode expr) {
    super(loc);
    this.expr = expr;
  }
}
