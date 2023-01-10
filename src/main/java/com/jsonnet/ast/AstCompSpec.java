package com.jsonnet.ast;

public class AstCompSpec {
  CompKind kind;
  String varName; // nil when kind != compSpecFor
  AstNode expr;

  public AstCompSpec(CompKind kind, String id, AstNode arr) {
    this.kind = kind;
    this.varName = id;
    this.expr = arr;
  }
}
