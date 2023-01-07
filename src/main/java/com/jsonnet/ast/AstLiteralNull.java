package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstLiteralNull represents the null keyword
class AstLiteralNull extends AstBaseNode {
  AstLiteralNull(LocationRange loc) {
    super(loc);
  }
}
