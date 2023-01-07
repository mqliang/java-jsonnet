package com.jsonnet.lexer.fodder;

// Fodder is stuff (spaces, comments, etc) that is usually thrown away by lexers/preprocessors but is kept so that
// source can be round tripped with full fidelity.
public class FodderElement {
  public FodderKind fodderKind;
  public String data;
  public FodderElement(FodderKind fodderKind, String data) {
    this.fodderKind = fodderKind;
    this.data = data;
  }
}
