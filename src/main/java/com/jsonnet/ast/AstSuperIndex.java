package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstSuperIndex represents the super[e] and super.f constructs.
//
// Either index or identifier will be set before de-sugaring. After de-sugaring, id will be null.
public class AstSuperIndex extends AstBaseNode{
  AstNode index;
  String id;

  public AstSuperIndex(LocationRange loc, AstNode index, String id) {
    super(loc);
    this.index = index;
    this.id = id;
  }
}
