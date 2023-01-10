package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstIndex represents both e[e] and the syntax sugar e.f.
// One of index and id will be nil before de-sugaring. After de-sugaring id will be nil.
public class AstIndex extends AstBaseNode {
  AstNode target;
  AstNode index;
  String id;

  public AstIndex(LocationRange loc, AstNode target, AstNode index, String id) {
    super(loc);
    this.target = target;
    this.index = index;
    this.id = id;
  }
}
