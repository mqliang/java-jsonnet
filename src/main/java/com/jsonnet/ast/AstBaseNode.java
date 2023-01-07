package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


public class AstBaseNode implements AstNode {
  LocationRange loc;

  AstBaseNode(LocationRange loc) {
    this.loc = loc;
  }

  @Override
  public LocationRange getLoc() {
    return loc;
  }
}
