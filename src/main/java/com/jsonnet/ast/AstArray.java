package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;
import java.util.LinkedList;
import java.util.List;


// AstArray represents array constructors [1, 2, 3].
public class AstArray extends AstBaseNode {
  List<AstNode> elements = new LinkedList<>();
  boolean trailingComma;

  public AstArray(LocationRange loc) {
    super(loc);
  }

  public AstArray(LocationRange loc, List<AstNode> elements, boolean trailingComma) {
    super(loc);
    this.elements = elements;
    this.trailingComma = trailingComma;
  }

}
