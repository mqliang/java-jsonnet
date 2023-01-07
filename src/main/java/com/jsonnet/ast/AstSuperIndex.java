package com.jsonnet.ast;

// AstSuperIndex represents the super[e] and super.f constructs.
//
// Either index or identifier will be set before desugaring. After desugaring, id will be
// nil.
public class AstSuperIndex {
  AstNode index;
  String id;
}
