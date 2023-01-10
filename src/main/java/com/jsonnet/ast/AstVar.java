package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstVar represents variables.
public class AstVar extends AstBaseNode{
  public String id;
  String original;

  public AstVar(LocationRange loc, String id, String original) {
    super(loc);
    this.id = id;
    this.original = original;
  }
}
