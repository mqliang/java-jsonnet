package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstConditional represents if/then/else.
// After parsing, branchFalse can be nil indicating that no else branch was specified. The desugarer fills this in
// with a LiteralNull
public class AstConditional extends AstBaseNode {
  AstNode cond;
  AstNode branchTrue;
  AstNode branchFalse;

  public AstConditional(LocationRange loc, AstNode cond, AstNode branchTrue, AstNode branchFalse) {
    super(loc);
    this.cond = cond;
    this.branchTrue = branchTrue;
    this.branchFalse = branchFalse;
  }
}
