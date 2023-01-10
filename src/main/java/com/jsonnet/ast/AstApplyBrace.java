package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstApplyBrace represents e { }.  Desugared to e + { }.
public class AstApplyBrace extends AstBaseNode {
  AstNode left;
  AstNode right;

  public AstApplyBrace(LocationRange loc, AstNode left, AstNode right) {
    super(loc);
    this.left = left;
    this.right = right;
  }
}
