package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;
import java.util.LinkedList;
import java.util.List;


// AstLocal represents local x = e; e.  After de-sugaring, functionSugar is false.
public class AstLocal extends AstBaseNode {
  List<AstLocalBind> binds = new LinkedList<>();
  AstNode body;

  AstLocal(LocationRange loc) {
    super(loc);
  }

  // AstLocalBind is a helper struct for astLocal
  static class AstLocalBind {
    String variable;
    AstNode body;
    boolean functionSugar;
    List<String> params = new LinkedList<>(); // if functionSugar is true
    boolean trailingComma;
  }
}
