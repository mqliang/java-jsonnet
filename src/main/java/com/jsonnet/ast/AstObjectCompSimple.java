package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstObjectCompSimple represents post-desugaring object comprehension: { [e]: e for x in e }.
public class AstObjectCompSimple extends AstBaseNode {
  AstNode field;
  AstNode value;
  String id;
  AstNode array;

  AstObjectCompSimple(LocationRange loc) {
    super(loc);
  }
}
