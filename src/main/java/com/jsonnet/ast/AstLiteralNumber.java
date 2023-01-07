package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstLiteralNumber represents a JSON number
class AstLiteralNumber extends AstBaseNode {
  double value;

  AstLiteralNumber(LocationRange loc) {
    super(loc);
  }
}
