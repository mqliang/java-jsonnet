package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;
import java.util.LinkedList;
import java.util.List;


// AstDesugaredObject represents object constructors { f: e ... } after desugaring.
// The assertions either return true or raise an error.
public class AstDesugaredObject extends AstBaseNode {
  List<AstNode> asserts = new LinkedList<>();
  List<AstDesugaredObjectField> fields = new LinkedList<>();

  AstDesugaredObject(LocationRange loc) {
    super(loc);
  }

  static class AstDesugaredObjectField {
    AstObjectFieldHide hide;
    AstNode name;
    AstNode body;
  }
}
