package com.jsonnet.ast;

public class AstCompSpec {
  CompKind kind;
  String varName; // nil when kind != compSpecFor
  AstNode expr;
}
