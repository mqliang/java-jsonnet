package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;
import java.util.LinkedList;
import java.util.List;


// AstFunction represents a function call.
public class AstFunction extends AstBaseNode {
  List<String> parameters = new LinkedList<>();
  boolean trailingComma;
  AstNode body;

  AstFunction(LocationRange loc) {
    super(loc);
  }
}
