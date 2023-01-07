package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


public class AstSelf extends AstBaseNode {
  AstSelf(LocationRange loc) {
    super(loc);
  }
}
