package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;
import java.util.LinkedList;
import java.util.List;


// AstLocal represents local x = e; e.  After de-sugaring, functionSugar is false.
public class AstLocal extends AstBaseNode {
  List<AstLocalBind> binds;
  AstNode body;

  public AstLocal(LocationRange loc,  List<AstLocalBind> binds, AstNode body) {
    super(loc);
    this.binds = binds;
    this.body = body;
  }
}
