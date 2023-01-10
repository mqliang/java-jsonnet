package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;
import java.util.LinkedList;
import java.util.List;
import org.checkerframework.checker.units.qual.A;


// AstApply represents a function call
public class AstApply extends AstBaseNode {
  AstNode target;
  List<AstNode> arguments;
  boolean trailingComma;
  boolean tailStrict;

  public AstApply(LocationRange loc, AstNode target, List<AstNode> arguments, boolean trailingComma,
      boolean tailStrict) {
    super(loc);
    this.target = target;
    this.arguments = arguments;
    this.trailingComma = trailingComma;
    this.tailStrict = tailStrict;
  }
}
