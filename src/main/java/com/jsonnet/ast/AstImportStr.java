package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstImport represents importstr "file".
public class AstImportStr extends AstBaseNode {
  String file;

  AstImportStr(LocationRange loc) {
    super(loc);
  }
}
