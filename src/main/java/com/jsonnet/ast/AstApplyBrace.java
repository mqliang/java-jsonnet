package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstApplyBrace represents e { }.  Desugared to e + { }.
public class AstApplyBrace extends AstBaseNode {
  AstNode left;
  AstNode right;

  AstApplyBrace(LocationRange loc) {
    super(loc);
  }
}
