package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;
import java.util.List;


// AstObject represents object constructors { f: e ... }.
// The trailing comma is only allowed if len(fields) > 0.
// Converted to DesugaredObject during de-sugaring.
public class AstObject extends AstBaseNode {
  List<AstObjectField> fields;
  boolean trailingComma;

  public AstObject(LocationRange loc, List<AstObjectField> fields, boolean trailingComma) {
    super(loc);
    this.fields = fields;
    this.trailingComma = trailingComma;
  }
}
