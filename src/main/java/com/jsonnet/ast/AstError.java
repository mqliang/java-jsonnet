package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstError represents the error e.
public class AstError extends AstBaseNode {
  AstNode expr;

  AstError(LocationRange loc) {
    super(loc);
  }
}
