package com.jsonnet.ast;

public enum UnaryOp {
  uopNot("!"), uopBitwiseNot("~"), uopPlus("+"), uopMinus("-");

  private final String text;

  UnaryOp(String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return text;
  }

  public static boolean contains(String text) {
    for (UnaryOp op : UnaryOp.values()) {
      if (op.name().equals(text)) {
        return true;
      }
    }
    return false;
  }
}
