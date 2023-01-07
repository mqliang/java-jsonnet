package com.jsonnet.ast;

public enum AstObjectFieldHide {
  astObjectFieldHidden, // f:: e
  astObjectFieldInherit, // f: e
  astObjectFieldVisible // f::: e
}
