package com.jsonnet.ast;

// BinaryOp represents binary operator kind
public enum BinaryOp {
  bopMult("*"), bopDiv("/"), bopPercent("%"),

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

  public static boolean contains(String text) {
    for (BinaryOp op : BinaryOp.values()) {
      if (op.name().equals(text)) {
        return true;
      }
    }
    return false;
  }
}
