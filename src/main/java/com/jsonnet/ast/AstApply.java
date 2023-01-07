package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;
import java.util.LinkedList;
import java.util.List;


// AstApply represents a function call
public class AstApply extends AstBaseNode {
  AstNode target;
  List<AstNode> arguments = new LinkedList<>();
  boolean trailingComma;
  boolean tailStrict;

  AstApply(LocationRange loc) {
    super(loc);
  }
}
