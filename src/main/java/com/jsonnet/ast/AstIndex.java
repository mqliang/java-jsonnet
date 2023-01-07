package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstIndex represents both e[e] and the syntax sugar e.f.
// One of index and id will be nil before de-sugaring. After de-sugaring id will be nil.
public class AstIndex extends AstBaseNode {
  AstNode target;
  AstNode index;
  String id;

  AstIndex(LocationRange loc) {
    super(loc);
  }
}
