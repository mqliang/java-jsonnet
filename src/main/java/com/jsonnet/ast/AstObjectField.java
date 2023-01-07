package com.jsonnet.ast;

import java.util.List;


public class AstObjectField {
  AstObjectFieldKind kind;
  AstObjectFieldHide hide; // (ignore if kind != astObjectField*)
  boolean superSugar; // +:  (ignore if kind != astObjectField*)
  boolean methodSugar; // f(x, y, z): ...  (ignore if kind  == astObjectAssert)
  AstNode expr1; // Not in scope of the object
  String id;
  List<String> ids; // If methodSugar == true then holds the params.
  boolean trailingComma; // If methodSugar == true then remembers the trailing comma
  // In scope of the object (can see self).
  AstNode expr2;
  AstNode expr3;
}
