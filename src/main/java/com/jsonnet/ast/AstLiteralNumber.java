package com.jsonnet.ast;

import com.jsonnet.lexer.LocationRange;


// AstLiteralNumber represents a JSON number
public class AstLiteralNumber extends AstBaseNode {
  double value;
  String originalString;

  public AstLiteralNumber(LocationRange loc, double value, String text) {
    super(loc);
    this.value =  value;
    this.originalString = text;
  }
}
