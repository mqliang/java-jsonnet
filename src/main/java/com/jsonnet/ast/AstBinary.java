package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstBinary represents binary operators.
public class AstBinary extends AstBaseNode {
  AstNode left;
  BinaryOp op;
  AstNode right;

  public AstBinary(LocationRange loc, AstNode left, BinaryOp op, AstNode right) {
    super(loc);
    this.left = left;
    this.op = op;
    this.right = right;
  }
}
