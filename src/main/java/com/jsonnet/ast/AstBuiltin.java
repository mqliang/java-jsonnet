package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;
import java.util.List;


// AstBuiltin represents built-in functions.
// There is no parse rule to build this AST. Instead, it is used to build the std object in the interpreter.
public class AstBuiltin extends AstBaseNode {
  int id;
  List<String> params;

  AstBuiltin(LocationRange loc) {
    super(loc);
  }
}
