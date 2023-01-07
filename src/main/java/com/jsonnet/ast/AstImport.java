package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstImport represents import "file".
public class AstImport extends AstBaseNode {
  String file;

  AstImport(LocationRange loc) {
    super(loc);
  }
}
