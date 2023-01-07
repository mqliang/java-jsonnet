package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstUnary represents unary operators.
public class AstUnary extends AstBaseNode {
  UnaryOp op;
  AstNode expr;

  AstUnary(LocationRange loc) {
    super(loc);
  }
}
