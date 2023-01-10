package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstUnary represents unary operators.
public class AstUnary extends AstBaseNode {
  UnaryOp op;
  AstNode expr;

  public AstUnary(LocationRange loc, UnaryOp op, AstNode expr) {
    super(loc);
    this.op = op;
    this.expr = expr;
  }
}
