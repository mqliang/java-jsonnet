package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;
import java.util.LinkedList;
import java.util.List;


// AstObject represents object constructors { f: e ... }.
// The trailing comma is only allowed if len(fields) > 0.
// Converted to DesugaredObject during de-sugaring.
public class AstObject extends AstBaseNode {
  List<AstObjectField> fields = new LinkedList<>();
  boolean trailingComma;

  AstObject(LocationRange loc) {
    super(loc);
  }
}
