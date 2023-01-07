package com.jsonnet.ast;

// BinaryOp represents binary operator kind
public enum BinaryOp {
  bopMul("*"), bopDiv("/"), bopPercent("%"),

  bopPlus("+"), bopMinus("-"),

  bopShiftL("<<"), bopShiftR(">>"),

  bopGreater(">"), bopGreaterEq(">="), bopLess("<"), bopLessEq("<="),

  bopManifestEqual("=="), bopManifestUnequal("!="),

  bopBitwiseAnd("&"), bopBitwiseXor("^"), bopBitwiseOr("|"),

  bopAnd("&&"), bopOr("||");

  private final String text;

  BinaryOp(String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return text;
  }
}
