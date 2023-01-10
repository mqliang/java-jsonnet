package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstImport represents import "file".
public class AstImport extends AstBaseNode {
  String file;

  public AstImport(LocationRange loc, String file) {
    super(loc);
    this.file = file;
  }
}
