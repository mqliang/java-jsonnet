package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;
import java.util.LinkedList;
import java.util.List;


// AstArray represents array constructors [1, 2, 3].
public class AstArray extends AstBaseNode {
  List<AstNode> elements = new LinkedList<>();
  boolean trailingComma;

  AstArray(LocationRange loc) {
    super(loc);
  }
}
