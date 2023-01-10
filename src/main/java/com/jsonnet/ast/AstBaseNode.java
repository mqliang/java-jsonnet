package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;
import lombok.AllArgsConstructor;


@AllArgsConstructor
abstract class AstBaseNode implements AstNode {
  final LocationRange loc;

  @Override
  public LocationRange getLoc() {
    return loc;
  }
}
