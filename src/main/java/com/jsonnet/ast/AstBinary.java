package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstBinary represents binary operators.
public class AstBinary extends AstBaseNode {
  AstNode left;
  BinaryOp op;
  AstNode right;

  AstBinary(LocationRange loc) {
    super(loc);
  }
}
