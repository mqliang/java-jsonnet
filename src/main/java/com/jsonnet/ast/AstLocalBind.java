package com.jsonnet.ast;

import java.util.List;
import lombok.AllArgsConstructor;


// AstLocalBind is a helper struct for astLocal
@AllArgsConstructor
public class AstLocalBind {
  public String variable;
  AstNode body;
  boolean functionSugar;
  List<String> params; // if functionSugar is true
  boolean trailingComma;

  public AstLocalBind(String variable, AstNode body) {
    this.variable = variable;
    this.body = body;
  }
}