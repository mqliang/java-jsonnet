package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;
import java.util.LinkedList;
import java.util.List;


// AstArrayComp represents array comprehensions (which are like Python list comprehensions)
public class AstArrayComp extends AstBaseNode {
  AstNode body;
  boolean trailingComma;
  List<AstCompSpec> spec = new LinkedList<>();

  AstArrayComp(LocationRange loc) {
    super(loc);
  }
}


