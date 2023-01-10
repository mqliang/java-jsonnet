package com.jsonnet.ast;

import java.util.List;


public class AstObjectField {
  public AstObjectFieldKind kind;
  public AstObjectFieldHide hide; // (ignore if kind != astObjectField*)
  boolean superSugar; // +:  (ignore if kind != astObjectField*)
  boolean methodSugar; // f(x, y, z): ...  (ignore if kind  == astObjectAssert)
  AstNode expr1; // Not in scope of the object
  String id;
  List<String> ids; // If methodSugar == true then holds the params.
  boolean trailingComma; // If methodSugar == true then remembers the trailing comma
  // In scope of the object (can see self).
  AstNode expr2;
  AstNode expr3;

  public AstObjectField(
      AstObjectFieldKind kind,
      AstObjectFieldHide hide,
      boolean superSugar,
      boolean methodSugar,
      AstNode expr1,
      String id,
      List<String> ids,
      boolean trailingComma,
      AstNode expr2,
      AstNode expr3) {
    this.kind = kind;
    this.hide = hide;
    this.superSugar = superSugar;
    this.methodSugar = methodSugar;
    this.expr1 = expr1;
    this.id = id;
    this.ids = ids;
    this.trailingComma = trailingComma;
    this.expr2 = expr2;
    this.expr3 = expr3;
  }
}
